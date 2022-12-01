import pandas as pd
import seaborn as sn
import matplotlib.pyplot as plt
from Data import *

default_fig_size = (2.7, 2)
Labelsizes = 11
legendSize = 8
tickssize = 9

def draw_1():
    ChengduWalk_AvgTimeDf = pd.DataFrame(ChengduWalk_AvgTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([20, 60])
    ax = sn.lineplot(data=ChengduWalk_AvgTimeDf, x="Maximum walking time (s)", y="Avg. walking time(s)", marker='o', dashes=False)
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    plt.tight_layout()
    plt.savefig("./img/Chengdu_walk_AvgTime" + ".eps", dpi=1200, format='eps')

def draw_2():
    ChengduDrive_AvgTimeDF = pd.DataFrame(ChengduDrive_AvgTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([20, 60])
    ax = sn.lineplot(data=ChengduDrive_AvgTimeDF, x="Maximum driving time (s)", y="Avg. walking time(s)", marker='o', dashes=False)
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    plt.tight_layout()
    plt.savefig("./img/Chengdu_drive_AvgTime" + ".eps", dpi=1200, format='eps')

def draw_3():
    ChengduPeriod_AvgTimeDF = pd.DataFrame(ChengduPeriod_AvgTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([20, 60])
    ax = sn.barplot(data=ChengduPeriod_AvgTimeDF, x="Different times of day", y="Avg. walking time(s)")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    plt.tight_layout()
    plt.savefig("./img/Chengdu_period_AvgTime" + ".eps", dpi=1200, format='eps')

def draw_4():
    NYCWalk_AvgTimeDF = pd.DataFrame(NYCWalk_AvgTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([20, 60])
    ax = sn.lineplot(data=NYCWalk_AvgTimeDF, x="Maximum walking time (s)", y="Avg. walking time(s)", marker='o', dashes=False)
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    plt.tight_layout()
    plt.savefig("./img/NYC_walk_AvgTime" + ".eps", dpi=1200, format='eps')

def draw_5():
    NYCDrive_AvgTimeDF = pd.DataFrame(NYCDrive_AvgTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([20, 60])
    ax = sn.lineplot(data=NYCDrive_AvgTimeDF, x="Maximum driving time (s)", y="Avg. walking time(s)", marker='o', dashes=False)
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    plt.tight_layout()
    plt.savefig("./img/NYC_drive_AvgTime" + ".eps", dpi=1200, format='eps')

def draw_6():
    NYCPeriod_AvgTimeDF = pd.DataFrame(NYCPeriod_AvgTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([20, 60])
    ax = sn.barplot(data=NYCPeriod_AvgTimeDF, x="Different times of day", y="Avg. walking time(s)")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    plt.tight_layout()
    plt.savefig("./img/NYC_period_AvgTime" + ".eps", dpi=1200, format='eps')

def draw_7():
    ChengduWalk_RunTimeDF = pd.DataFrame(ChengduWalk_RunTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0,\
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['legend.frameon'] = False
    plt.rcParams['ytick.direction'] = 'in'
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([0, 500])
    ax = sn.lineplot(data=ChengduWalk_RunTimeDF, x="Maximum walking time (s)", y="Avg. running time(ms)", markers=True, dashes=False, hue="Algorithm", style="Algorithm")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    ax.get_legend().set_title(None)
    plt.tight_layout()
    plt.savefig("./img/Chengdu_walk_RunTime" + ".eps", dpi=1200, format='eps')

def draw_8():
    ChengduDrive_RunTimeDF = pd.DataFrame(ChengduDrive_RunTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    plt.rcParams['legend.frameon'] = False
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([0, 500])
    ax = sn.lineplot(data=ChengduDrive_RunTimeDF, x="Maximum driving time (s)", y="Avg. running time(ms)", markers=True, dashes=False, hue="Algorithm", style="Algorithm")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    ax.get_legend().set_title(None)
    plt.tight_layout()
    plt.savefig("./img/Chengdu_drive_RunTime" + ".eps", dpi=1200, format='eps')

def draw_9():
    ChengduPeriod_RunTimeDF = pd.DataFrame(ChengduPeriod_RunTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    plt.rcParams['legend.frameon'] = False
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([0, 400])
    ax = sn.barplot(data=ChengduPeriod_RunTimeDF, x="Different times of day", y="Avg. running time(ms)", hue="Algorithm")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    ax.get_legend().set_title(None)
    plt.tight_layout()
    plt.savefig("./img/Chengdu_period_RunTime" + ".eps", dpi=1200, format='eps')

def draw_10():
    NYCWalk_RunTimeDF = pd.DataFrame(NYCWalk_RunTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    plt.rcParams['legend.frameon'] = False
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([0, 700])
    ax = sn.lineplot(data=NYCWalk_RunTimeDF, x="Maximum walking time (s)", y="Avg. running time(ms)", markers=True, dashes=False, hue="Algorithm", style="Algorithm")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    ax.get_legend().set_title(None)
    plt.tight_layout()
    plt.savefig("./img/NYC_walk_RunTime" + ".eps", dpi=1200, format='eps')

def draw_11():
    NYCDrive_RunTimeDF = pd.DataFrame(NYCDrive_RunTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    plt.rcParams['legend.frameon'] = False
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([0, 700])
    ax = sn.lineplot(data=NYCDrive_RunTimeDF, x="Maximum driving time (s)", y="Avg. running time(ms)", markers=True, dashes=False, hue="Algorithm", style="Algorithm")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    ax.get_legend().set_title(None)
    plt.tight_layout()
    plt.savefig("./img/NYC_drive_RunTime" + ".eps", dpi=1200, format='eps')

def draw_12():
    NYCPeriod_RunTimeDF = pd.DataFrame(NYCPeriod_RunTime)
    sn.set(font_scale=3, font='Times New Roman')
    sn.set_style("whitegrid", {"axes.grid": False}) 
    paper_rc = {'lines.linewidth': 1, 'lines.markersize': Labelsizes, "axes.labelsize": Labelsizes, "grid.alpha": 1, "axes.grid": False, \
        "legend.fontsize": legendSize, "xtick.labelsize": tickssize, "ytick.labelsize":tickssize, "legend.title_fontsize": 0, \
             "xtick.bottom" : True, "ytick.left" : True, "xtick.direction": "in", "ytick.direction": "in"}                  
    sn.set_context("paper", rc = paper_rc)
    plt.rcParams['xtick.direction'] = 'in'
    plt.rcParams['ytick.direction'] = 'in'
    plt.rcParams['legend.frameon'] = False
    ##############################
    plt.figure(figsize=default_fig_size)
    plt.ylim([0, 400])
    ax = sn.barplot(data=NYCPeriod_RunTimeDF, x="Different times of day", y="Avg. running time(ms)", hue="Algorithm")
    ax.tick_params(bottom=True, top=False, left=True, right=False)
    ax.get_legend().set_title(None)
    plt.tight_layout()
    plt.savefig("./img/NYC_period_RunTime" + ".eps", dpi=1200, format='eps')

draw_1()
draw_2()
draw_3()
draw_4()
draw_5()
draw_6()
draw_7()
draw_8()
draw_9()
draw_10()
draw_11()
draw_12()

