# Web frontend for Masked-Intelligence

## Dependencies
`vite` is the only dependncy at the moment, and likely will remain the only one.
To install, npm packages, run
```sh
npm install
```

Vite is useful for its HMR (Hot Module Replacement), and its optimization of static assets for production.

By default Vite prefers having a single `index.html` as an entrypoint for the application, but for simplicity we build a multi-page webapp.

## Running the web client

To check out the frontend with vite, run
```sh
npm run dev
```

To build the applicaiton in a `dist/` directory, run
```sh
npm run build
```
From the `vite.config.js` configuration file, we can see this will create a multi-page application in `dist/`, which you can set as root for an `nginx` server, for example.

When adding an `html` file in root, don't forget to point to it in `vite.config.js` or it will not be built when running ```sh npm run build```.

## TODO
Use flexbox to align image and prompt text.
