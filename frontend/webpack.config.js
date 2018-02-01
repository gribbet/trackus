const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const UglifyJSPlugin = require("uglifyjs-webpack-plugin");

module.exports = (env = {}) => ({
    devtool: env.production ? "" : "source-map",
    context: path.resolve(__dirname, "source"),
    entry: "./scripts/index.ts",
    output: {
        path: path.resolve(__dirname, "build"),
        filename: env.production ? "index.[hash].js" : "index.js",
        publicPath: "/"
    },
    resolve: {
        extensions: [".ts", ".js", ".pug", ".pcss"]
    },
    module: {
        loaders: [{
            test: /\.ts$/,
            exclude: /node_modules/,
            loader: "ts-loader"
        }, {
            test: /\.pcss$/,
            loader: "postcss-loader",
            options: {
                sourceMap: env.production ? "" : "inline",
                plugins: () => [
                    require("postcss-import"),
                    require("postcss-mixins"),
                    require("postcss-cssnext"),

                ].concat(env.production
                    ? require("cssnano")({
                        autoprefixer: false
                    })
                    : [])
            }
        }, {
            test: /\.pug$/,
            loaders: ["html-loader", "pug-html-loader"]
        }, {
            test: /\.js$/,
            loader: "source-map-loader",
            enforce: "pre"
        }]
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: "templates/index.pug"
        })
    ].concat(env.production ? new UglifyJSPlugin() : [])
});