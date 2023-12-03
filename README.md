# Business Pulse REST API

Welcome to Business Pulse REST API! This API provides a powerful and flexible solution for managing company data, including departments, employees, and timecards.

## Features

- **Company Services**: Perform CRUD operations on company data.
- **Department Management**: Manage department details with ease.
- **Employee Tracking**: Keep track of employee information and activities.
- **Timecard System**: Efficiently handle employee timecards.

## Technologies Used

- Java
- Jakarta EE
- [Gson](https://github.com/google/gson) for JSON processing

## Getting Started

To get started with Business Pulse REST API, follow these steps:

1. Clone the repository: `git clone https://github.com/gwikina/BusinessPulseREST.git`
2. Set up your development environment.
3. Run the application and explore the API endpoints.

## API Endpoints

- **Company Services**
  - `GET /CompanyServices`: Get a simple welcome message.
  - `DELETE /CompanyServices/company`: Delete company information.

- **Department Management**
  - `GET /CompanyServices/department?company={company}&dept_id={id}`: Get department details.
  - `GET /CompanyServices/departments?company={company}`: Get all departments.
  - `PUT /CompanyServices/department`: Update department information.
  - `POST /CompanyServices/department`: Add a new department.
  - `DELETE /CompanyServices/department?company={company}&dept_id={id}`: Delete a department.

- **Employee Tracking**
  - `GET /CompanyServices/employee?emp_id={id}`: Get employee details.
  - `GET /CompanyServices/employees?company={company}`: Get all employees.
  - `PUT /CompanyServices/employee`: Update employee information.
  - `POST /CompanyServices/employee`: Add a new employee.
  - `DELETE /CompanyServices/employee?emp_id={id}`: Delete an employee.

- **Timecard System**
  - `GET /CompanyServices/timecard?timecard_id={id}`: Get timecard details.
  - `GET /CompanyServices/timecards?emp_id={id}`: Get all timecards for an employee.
  - `POST /CompanyServices/timecard`: Add a new timecard.
  - `PUT /CompanyServices/timecard`: Update timecard information.
  - `DELETE /CompanyServices/timecard?timecard_id={id}`: Delete a timecard.

## Contributing

We welcome contributions! Feel free to open issues or pull requests.

## License

This project is licensed under the [MIT License](LICENSE).
