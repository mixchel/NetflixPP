from maze import Maze
import maze as m
from collections import deque
import time
import os
from heapq import heappop, heappush


def print_statistics(best_maze, start_time, max_size_stack, n_path_percurred, mode):
    if best_maze.there_is_no_solution():
        os.system('clear')
        print(f"Modo selecionado: {mode}\n\n") 
        m.print_final_maze(best_maze)      
        print("Não foi encontrado solução!")    
    if best_maze.is_complete():   
        os.system('clear')
        print(f"Modo selecionado: {mode}\n\n") 
        m.print_final_maze(best_maze)
        print("Solução encontrada!")   
    print("\nMovimentos feitos:", len(best_maze.position_record))
    print(f"Tempo execução: {time.time() - start_time:.4f} segundos")
    print(f"Nós máximos na memória: {max_size_stack}")
    print(f"Nós filhos explorados: {n_path_percurred}")


def breadth_first_search(initial_maze):
    start_time = time.time()
    n_path_percurred = 0
    best_maze = initial_maze
    max_size_queue = 0
    queue = deque([initial_maze])  
    visited = set()
    while queue:
        if len(queue) > max_size_queue: max_size_queue = len(queue)
        maze = queue.popleft()   
        if len(maze.position_record) > len(best_maze.position_record): best_maze = maze
        visited.add(maze)
        if maze.is_complete():
                break
        for child in maze.children():   
            n_path_percurred+=1
            queue.append(child)    
            if child not in visited:
                queue.append(child)  
    print_statistics(best_maze, start_time, max_size_queue, n_path_percurred, 'DFS')
    return best_maze


def depth_first_search(initial_maze: Maze):
    start_time = time.time()
    n_path_percurred = 0
    best_maze = initial_maze
    max_size_stack = 0
    stack = deque([initial_maze]) 
    visited = set()
    while stack:
        if len(stack) > max_size_stack: max_size_stack = len(stack)
        maze = stack.pop() 
        if len(maze.position_record) > len(best_maze.position_record): best_maze = maze
        visited.add(maze)
        if maze.is_complete():
                break
        for child in maze.children():
            n_path_percurred+=1
            if child not in visited:
                stack.append(child)  
    print_statistics(best_maze, start_time, max_size_stack, n_path_percurred, 'DFS')
    return best_maze


def iterative_deeppening_search(initial_maze):
    start_time = time.time()
    n_path_percurred = 0
    depth_limit = 44
    best_maze = initial_maze
    for depth in range(1,depth_limit+1):
        best_maze, max_size_stack, n_path_percurred = depth_limited_search(initial_maze,depth, n_path_percurred)
        if best_maze.is_complete():
            break
    print_statistics(best_maze, start_time, max_size_stack, n_path_percurred, 'IFS')
    return best_maze


def depth_limited_search(initial_maze, limit, n_path_percurred):
    best_maze = initial_maze
    max_size_stack = 0
    stack = deque([(initial_maze, 0)]) 
    visited = set()
    while stack:
        if len(stack) > max_size_stack: max_size_stack = len(stack)
        maze, depth = stack.pop() 
        if len(maze.position_record) > len(best_maze.position_record): best_maze = maze
        if depth < limit:
            if maze.is_complete():
                break
            visited.add(maze)
            for child in maze.children():
                n_path_percurred+=1
                if child not in visited:
                    stack.append([child, depth + 1]) 
        else: break 
    return (best_maze, max_size_stack, n_path_percurred)   


def greedy_search(initial_maze, heuristic, cost=False, weight=0):
    start_time = time.time()
    n_path_percurred = 0
    priority_queue = []
    heappush(priority_queue, (heuristic(initial_maze, weight, cost=False), 0, initial_maze)) if not cost else heappush(priority_queue, (heuristic(initial_maze, weight, cost=True) , 0, initial_maze))
    visited = set()
    best_maze = initial_maze
    id_counter = 1
    max_size_heap = 0
    while priority_queue:
        if len(priority_queue) > max_size_heap: max_size_heap = len(priority_queue)
        _, _, maze = heappop(priority_queue)
        n_path_percurred += 1
        if len(maze.position_record) > len(best_maze.position_record): best_maze = maze
        visited.add(maze)
        if maze.is_complete():
            break 
        for child in maze.children():
            
            if child not in visited:
                heappush(priority_queue, (heuristic(child, weight), id_counter, child)) if not cost else heappush(priority_queue, (heuristic(child, weight, cost=True), id_counter, child))
                id_counter += 1
    mode = 'Greedy' 
    mode = 'A*' if cost and weight == 0 else 'Weighted A*'
    print_statistics(best_maze, start_time, max_size_heap, n_path_percurred, mode)
    return best_maze








