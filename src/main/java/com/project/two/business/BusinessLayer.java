package com.project.two.business;

import java.util.ArrayList;

import companydata.*; // use data layer
import jakarta.ws.rs.core.Response;

public class BusinessLayer {
    private DataLayer dataLayer;
    public BusinessLayer() {
        // Initialize your data layer or any other necessary components here
        dataLayer = new DataLayer("glw3325");
    }

    public void closeDataLayer() {
        // Close resources related to the data layer
        if (dataLayer != null) {
            dataLayer.close();
        }
    }
       public Response validateAndDeleteCompany(String company) {
        try {
            dataLayer.deleteCompany(company);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error: Cannot delete " + company)
                    .build();
        } finally {
            closeDataLayer();
        }
        return Response.ok("{\"success\":\"" + company + "'s information deleted.\"}").build();
    }
   


}
