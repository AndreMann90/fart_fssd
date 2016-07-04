import plotly.graph_objs as go
import plotly
import csv

with open("data.csv") as f: # TODO get file location as parameter
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

    plotly.offline.plot(data, filename='timeseries.html')
