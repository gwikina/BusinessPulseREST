package com.project.two;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import companydata.*;
import com.project.two.business.BusinessLayer;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("CompanyServices")
public class CompanyServices {

    private DataLayer dataLayer = null;
    private Department dept = new Department();
    private Employee emp = null;
    private Timecard tc = null;
    private Gson gson = new Gson();
    private BusinessLayer businessLayer = new BusinessLayer();

    @Context
    UriInfo uriInfo;

    /**
     * Method handataLayering HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @Path("/company")
    /**
     * Method handataLayering HTTP DELETE requests for deleting company information.
     * The returned object will be sent to the client as JSON.
     *
     * @param company the path parameter representing the company name
     * @return Response with JSON indicating success or failure
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCompany(@QueryParam("company") String company) {
        return businessLayer.validateAndDeleteCompany(company);
    }

    @Path("/department")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDepartment(@QueryParam("company") String company, @QueryParam("dept_id") int id) {

        try {
            dataLayer = new DataLayer("glw3325");
            dept = dataLayer.getDepartment(company, id);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        return Response.ok(gson.toJson(dept)).build();
    }

    @Path("/departments")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCompanyDepartments(@QueryParam("company") String company) {
        StringBuilder response = new StringBuilder("[");
        try {
            dataLayer = new DataLayer("glw3325");
            List<Department> departments = dataLayer.getAllDepartment(company);

            for (int i = 0; i < departments.size(); i++) {
                Department d = departments.get(i);
                response.append(gson.toJson(d));

                // Add a comma if it's not the last department
                if (i < departments.size() - 1) {
                    response.append(",");
                }
            }

            response.append("]");
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        return Response.ok(response.toString()).build();
    }

    @Path("/department")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDepartment(String json) {
        dept = gson.fromJson(json, Department.class);
        String company = dept.getCompany();
        int id = dept.getId();
        String no = dept.getDeptNo();
        String deptName = dept.getDeptName();
        String loc = dept.getLocation();
        Department existingDepartment = null;
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getDepartment(company, id);

            existingDepartment = dataLayer.getDepartmentNo(company, no);
            if (existingDepartment != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Error: Department with this 'dept_no' already exists. #"
                                + dataLayer.getDepartmentNo(company, no).getDeptNo())
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: Department does not exist.").build();
        } finally {
            dataLayer.close();
        }

        // Update
        try {
            dataLayer = new DataLayer("glw3325");
            existingDepartment = dataLayer.getDepartment(company, id);
            if (existingDepartment == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Error: No with this 'dept_id' exists.")
                        .build();
            }
            existingDepartment.setDeptName(deptName);
            existingDepartment.setDeptNo(no);
            existingDepartment.setLocation(loc);
            dataLayer.updateDepartment(existingDepartment);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to update.").build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + gson.toJson(dept) + "}").build();
    }

    @Path("/department")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDepartment(@FormParam("company") String company, @FormParam("dept_name") String name,
            @FormParam("dept_no") String no, @FormParam("location") String loc) {
        dept = new Department(company, name, no, loc);
        // Check if dept_no exists already (we don't want this)
        try {
            dataLayer = new DataLayer("glw3325");
            Department existingDepartment = dataLayer.getDepartment(company, Integer.parseInt(no));
            if (existingDepartment != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Error: Department with this 'dept_no' already exists.")
                        .build();
            }
        } catch (Exception e) {
            // Do nothing
        } finally {
            dataLayer.close();
        }
        // Add
        try {
            dataLayer = new DataLayer("glw3325");
            dept = dataLayer.insertDepartment(dept);
            if (dept.getId() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Failed to add.").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to add.").build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + gson.toJson(dept) + "}").build();
    }

    @Path("/department")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDepartment(@QueryParam("company") String company, @QueryParam("dept_id") int id) {
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.deleteDepartment(company, id);
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + id + " from " + company + " deleted.\"}").build();
    }

    @Path("/employee")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmployee(@QueryParam("emp_id") int id) {
        try {
            dataLayer = new DataLayer("glw3325");
            emp = dataLayer.getEmployee(id);
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } finally {
            dataLayer.close();
        }
        return Response.ok(gson.toJson(emp)).build();
    }

    @Path("/employees")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCompanyEmployees(@QueryParam("company") String company) {
        String response = "[";
        try {
            dataLayer = new DataLayer("glw3325");
            List<Employee> employees = dataLayer.getAllEmployee(company);

            // Check if the employees list is empty
            if (employees.isEmpty()) {
                // Construct an appropriate response for an empty list
                return Response.ok("[]").build();
            }

            // Loop through the employees and construct the JSON response
            for (Employee e : employees) {
                response += gson.toJson(e) + ",";
            }

            // Remove the trailing comma and close the JSON array
            if (response.endsWith(",")) {
                response = response.substring(0, response.length() - 1);
            }
            response += "]";
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        return Response.ok(response).build();
    }

    @Path("/employee")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEmployee(String json) {
        emp = gson.fromJson(json, Employee.class);

        int empId = emp.getId();
        String no = emp.getEmpNo();
        Date date = emp.getHireDate();
        int deptId = emp.getDeptId();

        // Get the company from the emp_no
        if (no == null)
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("error: Employee does not exist. for employee: " + emp.getId()).build();

        String company = "";

        // Check if emp_no has the correct format
        if (no.startsWith("glw3325-")) {
            String xxxPart = no.substring(8); // Extract the xxx part after "glw3325-"

            // Check if xxxPart is not longer than 15 characters
            if (xxxPart.length() <= 15) {
                company = no.substring(0, 7); // Extract the company part including "glw3325-"
            } else {
                // Handle the case where xxxPart is too long
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("error: Invalid emp_no format. Maximum length of xxx is 15 characters: " + no)
                        .build();
            }
        } else {
            // Handle the case where emp_no does not have the correct format
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("error: Invalid emp_no format. Must be in 'glw3325-xxx' (at most 15 xs) format: " + no)
                    .build();
        }

        // Check if employee exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getEmployee(empId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: Employee does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check if dept_id exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getDepartment(company, deptId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: 'dept_id' does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check if mng_id exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            // dataLayer.getEmployee(company, mngId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: 'mng_id' does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check date is not after current date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (!inputDateFormat.format(date).equals(dateFormat.format(date))) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date format. Use yyyy-MM-dd.").build();
        }
        Date currentDate = new Date(System.currentTimeMillis());
        if (date.after(currentDate)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Date cannot be in the future.").build();
        }
        // Check date is not a weekend
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Date cannot be on the weekend.").build();
        }
        // Update
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.updateEmployee(emp);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to update.").build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + gson.toJson(emp) + "}").build();
    } // end updateEmployee

    @Path("/employee")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEmployee(@FormParam("company") String company,
            @FormParam("emp_name") String name,
            @FormParam("emp_no") String no,
            @FormParam("hire_date") Date date,
            @FormParam("job") String job,
            @FormParam("salary") Double salary,
            @FormParam("dept_id") int deptId,
            @FormParam("mng_id") int mngId) {
        emp = new Employee(name, no, date, job, salary, deptId, mngId);
        // Check if dept_id exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getDepartment(company, deptId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: 'dept_id' does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check if mng_id exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getEmployee(mngId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: 'mng_id' does not exist.").build();
        } finally {
            dataLayer.close();
        }
        Date currentDate = new Date(System.currentTimeMillis());
        if (date.after(currentDate)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Date cannot be in the future.").build();
        }
        // Check date is not a weekend
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Date cannot be on the weekend.").build();
        }
        // Add
        try {
            dataLayer = new DataLayer("glw3325");
            emp = dataLayer.insertEmployee(emp);
            if (emp.getId() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Failed to add.").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to add.").build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + gson.toJson(emp) + "}").build();
    }

    @Path("/employee")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEmployee(@QueryParam("emp_id") int id) {
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.deleteEmployee(id);
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + id + " deleted.\"}").build();
    } // end deleteEmployee

    @Path("/timecard")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimecardId(@QueryParam("timecard_id") int id) {
        try {
            dataLayer = new DataLayer("glw3325");
            tc = dataLayer.getTimecard(id);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"timecard\":" + gson.toJson(tc) + "}").build();
    } // end getTimecardId

    @Path("/timecards")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimecardsId(@QueryParam("emp_id") int id) {
        StringBuilder response = new StringBuilder("[");
        try {
            dataLayer = new DataLayer("glw3325");
            List<Timecard> timecards = dataLayer.getAllTimecard(id);

            // Check if the timecards list is empty
            if (timecards.isEmpty()) {
                // Construct an appropriate response for an empty list
                return Response.ok("[]").build();
            }

            // Loop through the timecards and construct the JSON response
            for (int i = 0; i < timecards.size(); i++) {
                Timecard t = timecards.get(i);
                response.append(gson.toJson(t));

                // Add a comma if it's not the last timecard
                if (i < timecards.size() - 1) {
                    response.append(",");
                }
            }

            response.append("]");
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        return Response.ok(response.toString()).build();
    }

    @Path("/timecard")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTimecard(@FormParam("company") String company, @FormParam("timecard_id") int tcId,
            @FormParam("emp_id") int empId,
            @FormParam("start_time") Timestamp start, @FormParam("end_time") Timestamp end) {
        if (!company.equals("glw3325"))
            return Response.status(Response.Status.BAD_REQUEST).entity("error: You MUST enter a valid company").build();
        tc = new Timecard(tcId, start, end, empId);
        Date startDate = new Date(start.getTime());
        Date endDate = new Date(end.getTime());
        // Check if timecard exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getTimecard(tcId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: Timecard does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check if employee exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getEmployee(empId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: Employee does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check start is equal to or one week before current date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Check if the start date is in the correct format
        DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (!inputDateFormat.format(startDate).equals(dateFormat.format(startDate))) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid start date format. Use yyyy-MM-dd.")
                    .build();
        }
        // Check start is equal to or one week before current date
        Date currentDate = new Date(System.currentTimeMillis());
        Calendar previousWeekDay = Calendar.getInstance();
        previousWeekDay.add(Calendar.WEEK_OF_YEAR, -1);
        Date weekBefore = new Date(previousWeekDay.getTimeInMillis());
        if (startDate.after(currentDate)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start time cannot be in the future.").build();
        }
        if (startDate.before(weekBefore)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start time cannot be later than a week old.")
                    .build();
        }

        // Check end is one hour greater than start and on same date
        long milliseconds1 = start.getTime();
        long milliseconds2 = end.getTime();
        long diff = milliseconds2 - milliseconds1;
        long hours = diff / (60 * 60 * 1000);
        if (!startDate.toLocalDate().equals(endDate.toLocalDate())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start and end time must be on the same day. " + startDate.toString() + " -> "
                            + endDate.toString())
                    .build();
        }
        if (hours < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start and end time must be at least an hour apart.").build();
        }
        // Check start and end date are not on a weekend
        Calendar cStart = Calendar.getInstance();
        cStart.setTime(startDate);
        int dayOfWeekStart = cStart.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeekStart == 6 || dayOfWeekStart == 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start date cannot be on the weekend.").build();
        }
        Calendar cEnd = Calendar.getInstance();
        cEnd.setTime(endDate);
        int dayOfWeekEnd = cEnd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeekEnd == 6 || dayOfWeekEnd == 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity("End date cannot be on the weekend.").build();
        }
        // Check start and end are between 06:00:00 and 18:00:00
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        String timeStart = hourFormat.format(startDate);
        int hourStart = Integer.parseInt(timeStart);
        if (hourStart < 6 || hourStart > 18) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start time must be between 06:00:00 and 18:00:00.").build();
        }
        String timeEnd = hourFormat.format(endDate);
        int hourEnd = Integer.parseInt(timeEnd);
        if (hourEnd < 6 || hourEnd > 18) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("End time must be between 06:00:00 and 18:00:00.").build();
        }
        // Check start date is not on the same day as another start date
        try {
            dataLayer = new DataLayer("glw3325");
            List<Timecard> timecards = new LinkedList<Timecard>();
            timecards = dataLayer.getAllTimecard(empId);
            for (Timecard t : timecards) {
                Timestamp startTemp = t.getStartTime();
                Date startDateTemp = new Date(startTemp.getTime());
                if (startDateTemp.compareTo(startDate) == 1) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Start date cannot be on the same date as another Timecard.").build();
                }
            }
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } finally {
            dataLayer.close();
        }
        // Add
        try {
            dataLayer = new DataLayer("glw3325");
            tc = dataLayer.insertTimecard(tc);
            if (tc.getId() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Failed to add.").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to add.").build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\":  " + gson.toJson(tc) + " }").build();
    } // end

    @Path("/timecard")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTimecard(String json) {
        tc = gson.fromJson(json, Timecard.class);
        int empId = tc.getId();
        Timestamp start = tc.getStartTime();
        Timestamp end = tc.getEndTime();
        Date startDate = new Date(start.getTime());
        Date endDate = new Date(end.getTime());
        // Check if employee exists (we want this)
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.getEmployee(empId);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("error: Employee does not exist.").build();
        } finally {
            dataLayer.close();
        }
        // Check start is equal to or one week before current date
        Date currentDate = new Date(System.currentTimeMillis());
        Calendar previousWeekDay = Calendar.getInstance();
        previousWeekDay.add(Calendar.WEEK_OF_YEAR, -1);
        Date weekBefore = new Date(previousWeekDay.getTimeInMillis());
        if (startDate.after(currentDate)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start time cannot be in the future.").build();
        }
        if (startDate.before(weekBefore)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start time cannot be later than a week old.")
                    .build();
        }
        // Check end is one hour greater than start and on same date
        long milliseconds1 = start.getTime();
        long milliseconds2 = end.getTime();
        long diff = milliseconds2 - milliseconds1;
        long hours = diff / (60 * 60 * 1000);
        if (!startDate.toLocalDate().equals(endDate.toLocalDate())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start and end time must be on the same day.")
                    .build();
        }
        if (hours < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start and end time must be at least an hour apart.").build();
        }
        // Check start and end date are not on a weekend
        Calendar cStart = Calendar.getInstance();
        cStart.setTime(startDate);
        int dayOfWeekStart = cStart.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeekStart == 6 || dayOfWeekStart == 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Start date cannot be on the weekend.").build();
        }
        Calendar cEnd = Calendar.getInstance();
        cEnd.setTime(endDate);
        int dayOfWeekEnd = cEnd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeekEnd == 6 || dayOfWeekEnd == 7) {
            return Response.status(Response.Status.BAD_REQUEST).entity("End date cannot be on the weekend.").build();
        }
        // Check start and end are between 06:00:00 and 18:00:00
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        String timeStart = hourFormat.format(startDate);
        int hourStart = Integer.parseInt(timeStart);
        if (hourStart < 6 || hourStart > 18) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start time must be between 06:00:00 and 18:00:00.").build();
        }
        String timeEnd = hourFormat.format(endDate);
        int hourEnd = Integer.parseInt(timeEnd);
        if (hourEnd < 6 || hourEnd > 18) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("End time must be between 06:00:00 and 18:00:00.").build();
        }
        // Check start date is not on the same day as another start date
        try {
            dataLayer = new DataLayer("glw3325");
            List<Timecard> timecards = new LinkedList<Timecard>();
            timecards = dataLayer.getAllTimecard(empId);
            for (Timecard t : timecards) {
                Timestamp startTemp = t.getStartTime();
                Date startDateTemp = new Date(startTemp.getTime());
                if (startDateTemp.compareTo(startDate) == 1) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Start date cannot be on the same date as another Timecard.").build();
                }
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        // Update
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.updateTimecard(tc);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to update.").build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + gson.toJson(tc) + "}").build();
    } // end

    @Path("/timecard")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTimecard(@QueryParam("timecard_id") int id) {
        try {
            dataLayer = new DataLayer("glw3325");
            dataLayer.deleteTimecard(id);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Something went wrong.")
                    .build();
        } finally {
            dataLayer.close();
        }
        return Response.ok("{\"success\": " + id + " deleted.\"}").build();
    } // end deleteTimecard
    /*
    
    
     */

} // end CompanyServices class
