def euclidean_distance(maze, weight, cost=False):
    x1, y1 = maze.current_position
    x2, y2 = maze.final_position
    return (x2 - x1)**2 + (y2 - y1)**2 if not cost else ((x2 - x1)**2 + (y2 - y1)**2)*weight + len(maze.position_record)


def manhattan_distance(maze, weight, cost=False):
    x1, y1 = maze.current_position
    x2, y2 = maze.final_position
    return abs(x2 - x1) + abs(y2 - y1) if not cost else (abs(x2 - x1) + abs(y2 - y1))*weight + len(maze.position_record)