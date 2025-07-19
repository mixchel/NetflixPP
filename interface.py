import pygame
import sys
import time
from maze import Maze

CELL_SIZE = 100
BOARD_WIDTH = 100
BOARD_HEIGHT = 100
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
RED = (255, 0, 0)
GREEN = (124, 252, 0)
GRAY = (200, 200, 200)
BLUE = (0,0,255)

class Interface:
    def __init__(self, maze: Maze, rows: int, columns: int):
        self.rows = columns # pygame interface is inverted
        self.columns = rows # pygame interface is inverted
        self.initial_position = (maze.rows-1,0)
        self.final_position = (0,maze.columns-1)
        self.positions = maze.position_record  
        self.block_positions = maze.block_positions
        self.is_solved = maze.is_complete()
        self.screen_height = CELL_SIZE * rows
        self.screen_width = CELL_SIZE * columns


def create_interface(maze: Maze, rows: int, columns: int) -> None:
    """Create the interface display"""
    pygame.init()
    interface = Interface(maze, rows, columns)
    screen = pygame.display.set_mode((interface.screen_height,interface.screen_width))
    pygame.display.set_caption("Unequal Length Maze")

    board = [[0] * BOARD_WIDTH for _ in range(BOARD_HEIGHT)]
    old_tuple = interface.initial_position
    positions_visited = [old_tuple]

    for pos in interface.positions:
        screen.fill(WHITE)
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()

        if pos != interface.final_position and board[pos[0]][pos[1]] == 2:
            board[old_tuple[0]][old_tuple[1]] = 1
        board[pos[0]][pos[1]] = 2
        draw_board(screen)
        draw_blocks(screen, interface.block_positions)
        positions_visited.append(pos)
        draw_trajectory(screen, positions_visited)
        draw_position(screen, interface.initial_position, BLUE)
        draw_position(screen, interface.final_position, GREEN)
        rotated_screen = pygame.transform.rotate(screen, -90)
        rotated_screen = pygame.transform.flip(screen,True, True)
        screen.blit(rotated_screen, (0, 0))
        pygame.display.flip()
        time.sleep(0.2)
        old_tuple = pos

    time.sleep(5)
    pygame.quit()
    sys.exit()

def draw_blocks(screen: any, block_positions: list) -> None:
    """Draw blocked positions into board"""
    for pos in block_positions:
        x, y = pos
        rect = pygame.Rect(x * CELL_SIZE + CELL_SIZE // 4, y * CELL_SIZE + CELL_SIZE // 4, CELL_SIZE // 2, CELL_SIZE // 2)
        draw_block(screen, rect)


def draw_block(surface: any, rect: any) -> None:
    """Draw single block positions into board"""
    pygame.draw.rect(surface, RED, rect)
    pygame.draw.line(surface, WHITE, rect.topleft, rect.bottomright, 6)
    pygame.draw.line(surface, WHITE, rect.topright, rect.bottomleft, 6)


def draw_board(screen: any) -> None:
    """Draw background board"""
    for y in range(BOARD_HEIGHT):
        for x in range(BOARD_WIDTH):
            pygame.draw.rect(screen, GRAY, (x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE), 1)
            if x == 0:
                pygame.draw.line(screen, GRAY, (x * CELL_SIZE, y * CELL_SIZE), (x * CELL_SIZE, (y + 1) * CELL_SIZE), 2)
            if x == BOARD_WIDTH - 1:
                pygame.draw.line(screen, GRAY, ((x + 1) * CELL_SIZE, y * CELL_SIZE), ((x + 1) * CELL_SIZE, (y + 1) * CELL_SIZE), 2)
            if y == 0:
                pygame.draw.line(screen, GRAY, (x * CELL_SIZE, y * CELL_SIZE), ((x + 1) * CELL_SIZE, y * CELL_SIZE), 2)
            if y == BOARD_HEIGHT - 1:
                pygame.draw.line(screen, GRAY, (x * CELL_SIZE, (y + 1) * CELL_SIZE), ((x + 1) * CELL_SIZE, (y + 1) * CELL_SIZE), 2)


def draw_position(screen: any, pos: int, color: tuple) -> None:
    """Draw some position into board, like initial and final positions"""
    x, y = pos
    rect = pygame.Rect(x * CELL_SIZE + CELL_SIZE // 4, y * CELL_SIZE + CELL_SIZE // 4, CELL_SIZE // 2, CELL_SIZE // 2)
    pygame.draw.rect(screen, color, rect)


def draw_trajectory(screen: any, positions: list) -> None:
    """Draw lines of the trajectory"""
    line_size = 31
    for i in range(len(positions)-1):
        start_point = (positions[i][0] * CELL_SIZE + CELL_SIZE // 2, positions[i][1] * CELL_SIZE + CELL_SIZE // 2)
        end_point = (positions[i + 1][0] * CELL_SIZE + CELL_SIZE // 2, positions[i + 1][1] * CELL_SIZE + CELL_SIZE // 2)
        pygame.draw.line(screen, BLACK, start_point, end_point, line_size)
        draw_line_edges(screen, start_point, end_point, line_size)
        
    
def draw_line_edges(screen: any, start_point: tuple, end_point: tuple, line_size: int) -> None:
    """Draw rectangles to fill trajectory edges"""
    rect_start = (start_point[0] - line_size // 2, start_point[1] - line_size // 2, line_size, line_size)
    rect_end = (end_point[0] - line_size // 2, end_point[1] - line_size // 2, line_size, line_size)
    pygame.draw.rect(screen, BLACK, rect_start)
    pygame.draw.rect(screen, BLACK, rect_end)
          

