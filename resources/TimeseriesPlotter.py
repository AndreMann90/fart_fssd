from __future__ import print_function
import plotly.graph_objs as go
import plotly
import csv
import sys


def plot(fname="data.csv", output="timeseries.html"):
    with open(fname) as f:
        reader = csv.reader(f)

        header = next(reader)[1:]  # header without first column (time)
        time = []
        series = {}
        for h in header:
            series[h] = []
        for row in reader:
            time.append(row[0])
            for i, h in enumerate(header):
                series[h].append(row[i+1])

        data = []
        for key in series.keys():
            trace = go.Scatter(
                x=time,
                y=series[key],
                mode='lines',
                name=key
            )
            data.append(trace)

        plotly.offline.plot(data, filename=output)

if __name__ == "__main__":
    if len(sys.argv) == 3:
        plot(sys.argv[1], sys.argv[2])
    elif len(sys.argv) == 2:
        plot(sys.argv[1])
    else:
        sys.stderr.write("Usage: {prog} input.csv [output.html]\n".format(prog=sys.argv[0]))
        sys.stderr.flush()
