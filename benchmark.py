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
        ["java", "-cp", get_bin_dir(), "hanabAI.Hanabi"],
        encoding="utf8",
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=True
    )
    match = re.search(r"The final score is (?P<score>\d+).", result.stdout)
    if match is None:
        print(result.stdout, file=sys.stderr)
        print(result.stdout, file=sys.stderr)
        raise Exception("Could not find score in stdout.")
    return int(match.group("score"))


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



def main(argv=None):
    """Entry points for the benchmarker."""
    argv = argv or sys.argv[1:]
    parser = argparse.ArgumentParser("""Agent benchmarker""")
    parser.add_argument("--players",
                        metavar="NUMBER_PLAYERS",
                        type=int,
                        required=True,
                        default=5,
                        nargs="?",
                        help="""Number of players in the game.""",
                        dest="number_players")
    parser.add_argument("--iterations",
                        metavar="ITERATIONS",
                        type=int,
                        required=True,
                        default=1000,
                        nargs="?",
                        help="""Number games to play.""",
                        dest="iterations")
    result = parser.parse_args(argv)

    compile()
    results = []
    for i in tqdm(range(result.iterations)):
        results.append((i, simulate_game(result.number_players)))

    for i, score in results:
        print(i, score)

    plt_histogram([score for i, score in results])

if __name__ == "__main__":
    main()
