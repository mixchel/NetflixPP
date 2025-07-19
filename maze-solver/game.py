import os
from maze import Maze
import algorithms as a
import time
from interface import Interface
import interface
from heuristics import manhattan_distance, euclidean_distance


def main() -> None:
    """Play maze"""
    game_mode = select_game_mode()
    rows = 4
    columns = 4
    block_positions = []
    maze = create_game(rows, columns, block_positions)
    maze = solve_maze(game_mode, maze)
    # interface.create_interface(maze, rows, columns)


def create_game(rows, columns, block_positions) -> Maze:
    """Create initial board maze"""
    return Maze(rows, columns, block_positions)


def select_game_mode() -> int:
    """Print interface to choose game mode"""
    game_modes = {1: "BFS", 2: "DFS", 3: "Iterative DFS", 4: "Greedy" , 5: "A*", 6: "Weighted A*"}
    while True:
        os.system('clear')
        print("Escolha um modo de resolução:")
        for mode, description in game_modes.items():
            print(f"{mode}- {description}")
        game_mode = int(input("\nDigite o número correspondente ao modo desejado: "))
        if game_mode in game_modes: 
            return game_mode            
        print("Por favor, digite um número correspondente a um modo de resolução válido.\n")

DECISION_ALGORITHMS = {1: a.breadth_first_search,
                       2: a.depth_first_search,
                       3: a.iterative_deeppening_search,
                       4: a.greedy_search,
                       5: a.greedy_search,
                       6: a.greedy_search}

def solve_maze(game_mode: int, maze: Maze) -> Maze:
    """Chose the algorithm to play"""
    if game_mode >= 4: result_maze = DECISION_ALGORITHMS[game_mode](maze, euclidean_distance)
    else: result_maze = DECISION_ALGORITHMS[game_mode](maze)
    return result_maze

