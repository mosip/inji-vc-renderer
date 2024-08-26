const path = require('path');

module.exports = {
    entry: './src/vcrenderer.js',
    output: {
        filename: 'injivcrenderer.bundle.js',
        path: path.resolve(__dirname, 'dist'),
        library: 'InjiVcRenderer',
        libraryTarget: 'umd',
        globalObject: 'this'
    },
    mode: 'development',
    devtool: 'source-map',
    externals: {
        '@mosip/pixelpass': 'commonjs @mosip/pixelpass' // Adjust as needed
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env']
                    }
                }
            }
        ]
    }
};
