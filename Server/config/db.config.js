import dotenv from "dotenv";
dotenv.config();
export const host = process.env.DB_HOST;
export const user = process.env.DB_USER;
export const password = process.env.DB_PASSWORD;
export const database = process.env.DB_NAME;
export const waitForConnections = true;
export const connectionLimit = 10;
export const queueLimit = 0;

console.log(process.env.DB_NAME);
