# FARM-MANAGEMENT-SYSTEM
A responsive Java Swing desktop application designed to streamline farm administration, employee authentication, and resource management with a clean, modern UI

## Database setup

The app talks to a MySQL database named `farm_management_system`. The data lives
in the MySQL server, not in this folder, so every PC that runs the app needs its
own database.

1. Install MySQL and start it.
2. Create the schema and seed data (safe to run again at any time):

   ```bash
   mysql -u root -p < schema.sql
   ```

   This creates the `users`, `expenses` and `crop_plans` tables and writes the
   16 reference expense costs per acre. Everything else in the database is left
   untouched; re-running it also corrects any expense cost that has drifted from
   the values in `CropPlanPage.EXPENSE_SEED`.

3. Create your credentials file — `db.properties` is gitignored, so a fresh
   clone will not have one:

   ```bash
   cp db.properties.example db.properties
   ```

   Then edit `db.properties` so the URL, username and password match your
   MySQL installation.

4. Run the app. Register an account on the register page, then log in.

### Moving an existing database to another PC

To take your data with you instead of starting fresh:

```bash
# on the old PC
mysqldump -u root -p farm_management_system > farm_dump.sql
# on the new PC (after running schema.sql or on a fresh MySQL install)
mysql -u root -p farm_management_system < farm_dump.sql
```

Alternatively, leave the database on one machine and point the other PC at it:
set `url=jdbc:mysql://<that-pc-ip>:3306/farm_management_system` in
`db.properties`, allow remote connections in MySQL, and open port 3306.
