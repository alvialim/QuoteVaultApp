#!/usr/bin/env python3
"""
Helper script to extract colors from Stitch design screenshots.
Run this script to identify color values in the screenshots.

Usage:
    python3 extract_colors.py app/screenshots/home_screen.png
"""

import sys
from PIL import Image
import numpy as np

def extract_colors(image_path, num_colors=10):
    """Extract dominant colors from an image."""
    img = Image.open(image_path)
    img = img.convert('RGB')
    
    # Resize for faster processing
    img.thumbnail((200, 200))
    
    # Get colors
    colors = img.getcolors(maxcolors=256*256*256)
    
    if not colors:
        print(f"Could not extract colors from {image_path}")
        return
    
    # Sort by frequency
    colors.sort(reverse=True, key=lambda x: x[0])
    
    print(f"\n=== Colors from {image_path} ===")
    print(f"Top {num_colors} most common colors:\n")
    
    for i, (count, rgb) in enumerate(colors[:num_colors], 1):
        hex_color = f"#{rgb[0]:02X}{rgb[1]:02X}{rgb[2]:02X}"
        print(f"{i}. {hex_color} - RGB({rgb[0]}, {rgb[1]}, {rgb[2]}) - Count: {count}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 extract_colors.py <image_path>")
        sys.exit(1)
    
    extract_colors(sys.argv[1])
