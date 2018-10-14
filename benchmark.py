# /benchmark.py
#
# Script to benchmark agent performance.
#
# See /LICENCE.md for Copyright information
"""Script to benchmark agent performance."""

import argparse

import sys

import os

import re

import subprocess

import matplotlib

import numpy as np

import seaborn as sns

import matplotlib.pyplot as plt

from glob import glob

from operator import itemgetter

from tqdm import tqdm


def run(argv, *args, **kwargs):
    """Wrap subprocess.run and log what commands it runs."""
    return subprocess.run(argv, *args, **kwargs)

def get_repo_dir():
    return os.path.dirname(os.path.abspath(__file__))

def get_bin_dir():
    return os.path.join(get_repo_dir(), "bin")

def get_source_files():
    return glob(
        os.path.join(get_repo_dir(), "src", "**", "*.java"),
        recursive=True
    )

def compile():
    run(["javac", "-d", get_bin_dir()] + get_source_files(), check=True)

def simulate_game(number_players):
    result = run(
        ["java", "-cp", get_bin_dir(), "hanabAI.Hanabi", str(number_players)],
        encoding="utf8",
        stdout=subprocess.PIPE,
        # stderr=subprocess.PIPE,
        check=True
    )
    match = re.search(r"The final score is (?P<score>\d+).", result.stdout)
    score = -1 if match is None else int(match.group("score"))
    return result, score


def plt_histogram(values):
    num_bins = 25 # bins for each possible score
    xs = np.array(values)
    fig, ax = plt.subplots()
    n, bins, patches = ax.hist(xs, range=(0, 26), bins=num_bins, density=True)
    ax.set_xlabel("Scores")
    ax.set_ylabel("Probability Density")
    ax.set_title("Performance")
    plt.savefig("results.svg", format="svg")
    plt.show()


def plt_histogram_grid(titles, sets_of_values):
    num_bins = 25 # bins for each possible score
    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(20, 15))
    plt.subplots_adjust(hspace=0.3, wspace=0.3)
    for title, ax, values in zip(titles, axes.flatten(), sets_of_values):
        xs = np.array(values)
        n, bins, patches = ax.hist(xs, range=(0, 26), bins=num_bins, density=True)
        ax.set_xlabel("Scores")
        ax.set_ylabel("Probability Density")
        ax.set_title(title + r' $\mu={},\ \sigma={}$'.format(xs.mean(), xs.std()))
    plt.savefig("results.svg", format="svg")
    plt.show()


def write_debug_log(game_no, game_proc, log_dir):
    os.makedirs(log_dir, exist_ok=True)
    with open(os.path.join(log_dir, "game-{}.txt".format(game_no)), "w+") as f:
        f.write(game_proc.stderr or "")
        f.write(game_proc.stdout or "")


def main(argv=None):
    """Entry points for the benchmarker."""
    argv = argv or sys.argv[1:]
    parser = argparse.ArgumentParser("""Agent benchmarker""")
    # parser.add_argument("--players",
    #                     metavar="NUMBER_PLAYERS",
    #                     type=int,
    #                     default=5,
    #                     nargs="?",
    #                     help="""Number of players in the game.""",
    #                     dest="number_players")
    parser.add_argument("--iterations",
                        metavar="ITERATIONS",
                        type=int,
                        default=50,
                        nargs="?",
                        help="""Number games to play.""",
                        dest="iterations")
    parser.add_argument("--debug-score",
                        metavar="DEBUG_SCORE",
                        type=int,
                        default=0,
                        help="""Score below which to dump game logs.""",
                        dest="debug_score")
    parser.add_argument("--debug-dump",
                        metavar="DEBUG_SCORE",
                        type=str,
                        default=os.path.join(get_repo_dir(), "logs"),
                        help="""Score below which to dump game logs.""",
                        dest="log_dir")
    result = parser.parse_args(argv)

    compile()

    titles = []
    sets_of_scores = []

    for n_players in tqdm(range(2, 6)):
        title = "{} Players ({} Iterations)".format(n_players, result.iterations)
        scores = []
        for i in tqdm(range(result.iterations)):
            proc, score = simulate_game(n_players)
            if score <= result.debug_score:
                write_debug_log(i, proc, result.log_dir)
                print("Wrote debug log for game {}".format(i), file=sys.stderr)
            if score == -1:
                continue
            scores.append(score)
        titles.append(title)
        sets_of_scores.append(scores)

    plt_histogram_grid(titles, sets_of_scores)

if __name__ == "__main__":
    main()
