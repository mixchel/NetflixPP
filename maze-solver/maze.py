import numpy as np
from copy import deepcopy
from copy import copy


FINAL = 'F'
INITIAL = 'S'
BLOCK = '□'
CURRENT_POS = '○'
UP_ARROW = '↑'
RIGHT_ARROW = '→'
DOWN_ARROW = '↓'
LEFT_ARROW = '←'
LIST_OF_ARROWS = [UP_ARROW, DOWN_ARROW, LEFT_ARROW, RIGHT_ARROW, INITIAL]


class Maze:
    def __init__(self, rows: int, columns: int, block_positions: list) -> None:
        self.rows = rows
        self.columns = columns
        self.final_position = (0,self.columns-1)
        self.initial_position = (self.rows-1,0)
        self.board = self.make_board(block_positions)
        self.position_record = []
        self.last_move = 'last' 
        self.current_move = 'current'
        self.length_move = 0
        self.last_length_move = 1
        self.current_position = self.initial_position
        self.last_position = self.initial_position
        self.block_positions = block_positions


    def __str__(self) -> str: 
        """Print board of actual maze"""
        horizontal_border = '+---' * self.columns + '+'
        output = ""
        for row in self.board:  
            output += horizontal_border + '\n'
            output += '| ' + ' | '.join(row) + ' |\n'
        output += horizontal_border + '\n'
        return output
    
    
    def add_final_position(self, board: np.ndarray) -> None:
        """Add the final position to the maze board"""
        board[0][self.columns - 1] = FINAL
    

    def add_initial_position(self, board: np.ndarray) -> None:
        """Add the initial position to the maze board"""
        board[self.rows-1][0] = INITIAL
    

    def add_block_positions(self, board: np.ndarray, block_positions: list) -> None:
        """Add the blocking positions to the maze board"""
        for pos in block_positions:
            board[pos[0]][pos[1]] = BLOCK


    def make_board(self, block_positions: list) -> np.ndarray:
        """Create the board for the maze"""
        board = np.full((self.rows, self.columns), ' ', dtype='object')
        self.add_initial_position(board)
        self.add_final_position(board)
        self.add_block_positions(board, block_positions)
        return board


    def children(self) -> list:
        """Get the possible moves of a certain state"""
        list_of_moves = self.avaiable_moves()
        children = []
        for move in list_of_moves:
            child = move()
            if child:
                child.position_record += [child.current_position]
                children.append(child)
        return children
    

    def avaiable_moves(self) -> list:
        """Return the possible move functions to be explored based on the current position"""
        row, col = self.current_position
        functions = [] # up, down, left, right
        if not ((row-1 < 0) or (self.board[row-1,col] == BLOCK) or (self.board[row-1, col] == FINAL and np.count_nonzero(self.board == ' ') > 0) or self.board[row-1, col] in LIST_OF_ARROWS): functions.append(self.up) 
        if not ((row+1 > self.rows-1) or (self.board[row+1,col] == BLOCK) or (self.board[row+1, col] == FINAL and np.count_nonzero(self.board == ' ') > 0) or self.board[row+1, col] in LIST_OF_ARROWS): functions.append(self.down)
        if not ((col-1 < 0) or (self.board[row,col-1] == BLOCK) or (self.board[row, col-1] == FINAL and np.count_nonzero(self.board == ' ') > 0) or self.board[row, col-1] in LIST_OF_ARROWS): functions.append(self.left)
        if not ((col+1 > self.columns-1) or (self.board[row,col+1] == BLOCK) or (self.board[row, col+1] == FINAL and np.count_nonzero(self.board == ' ') > 0) or self.board[row, col+1] in LIST_OF_ARROWS): functions.append(self.right)
        return functions


    def update_length_moves(self, sign: str) -> None:
        """Update values of move length"""
        if self.current_move == sign: self.length_move+=1
        else:
            self.last_move = self.current_move
            self.current_move = sign
            self.last_length_move = self.length_move
            self.length_move = 1
    

    def move(func) -> np.ndarray | None:
        """Decorator for movements"""
        def decorator(self):
            node = deepcopy(self)
            # node = copy(self)
            result = func(node)  
            return node if result else None
        return decorator
    

    @move
    def up(self) -> bool:
        """Up movement and validations"""
        row, col = (self.current_position[0] - 1, self.current_position[1])
        if (self.current_move != UP_ARROW and self.length_move == self.last_length_move): 
            return False
        self.current_position = (row,col)
        self.board[row][col] = UP_ARROW
        self.update_length_moves(UP_ARROW)
        return True
        

    @move
    def down(self) -> bool:
        """Down movement and validations"""
        row, col = (self.current_position[0] + 1, self.current_position[1])
        if (self.current_move != DOWN_ARROW and self.length_move == self.last_length_move): 
             return False
        self.current_position = (row,col)
        self.board[row][col] = DOWN_ARROW
        self.update_length_moves(DOWN_ARROW)
        return True


    @move
    def left(self) -> bool:
        """Left movement and validations"""
        row, col = (self.current_position[0], self.current_position[1] - 1)
        if (self.current_move != LEFT_ARROW and self.length_move == self.last_length_move): 
            return False
        self.current_position = (row,col)
        self.board[row][col] = LEFT_ARROW
        self.update_length_moves(LEFT_ARROW)
        return True


    @move
    def right(self) -> bool:
        """Right movement and validations"""
        row, col = (self.current_position[0], self.current_position[1] + 1)
        if (self.current_move != RIGHT_ARROW and self.length_move == self.last_length_move): 
            return False
        self.current_position = (row,col)
        self.board[row][col] = RIGHT_ARROW
        self.update_length_moves(RIGHT_ARROW)
        return True


    def is_complete(self) -> bool:
        """Check if maze is solved"""
        return (self.current_position == self.final_position and np.count_nonzero(self.board == ' ') == 0)

    def there_is_no_solution(self) -> bool:
        """Check if maze is not solvable"""
        return self.children() == [] and (np.count_nonzero(self.board == ' ') > 0 or np.count_nonzero(self.board == ' ') == 0)
        

def print_final_maze(maze) -> None:
    """Print solved maze board"""
    final_board = copy(maze.board)
    # final_board = deepcopy(maze.board)
    final_board[0][maze.columns-1] = FINAL
    print(str_for_boards(final_board))
    

def str_for_boards(board) -> str:
    """Print board of actual maze"""
    rows, columns = len(board), len(board[0])
    horizontal_border = '+---' * columns + '+'
    output = ""
    for row in board:
        output += horizontal_border + '\n'
        output += '| ' + ' | '.join(row) + ' |\n'
    output += horizontal_border + '\n'
    return output
