import os
import shutil
import zipfile
import xml.etree.ElementTree as ET

W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
ET.register_namespace('w', W_NS)

def qn(tag):
    return f"{{{W_NS}}}{tag}"

def create_element(tag, **attribs):
    el = ET.Element(qn(tag))
    for k, v in attribs.items():
        el.set(qn(k), v)
    return el

def sub_element(parent, tag, **attribs):
    el = ET.SubElement(parent, qn(tag))
    for k, v in attribs.items():
        el.set(qn(k), v)
    return el

# --- Helper functions for OpenXML elements ---

def make_page_break():
    p = create_element('p')
    r = sub_element(p, 'r')
    sub_element(r, 'br', type='page')
    return p

def make_heading_1(text):
    p = create_element('p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'pStyle', val='Heading1')
    sub_element(pPr, 'spacing', before='300', after='140')
    sub_element(pPr, 'jc', val='left')
    
    r = sub_element(p, 'r')
    rPr = sub_element(r, 'rPr')
    sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman', cs='Times New Roman')
    sub_element(rPr, 'b')
    sub_element(rPr, 'sz', val='32') # 16pt
    sub_element(rPr, 'szCs', val='32')
    sub_element(rPr, 'color', val='003366') # Navy
    t = sub_element(r, 't')
    t.text = text
    return p

def make_heading_2(text):
    p = create_element('p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'pStyle', val='Heading2')
    sub_element(pPr, 'spacing', before='220', after='100')
    sub_element(pPr, 'jc', val='left')
    
    r = sub_element(p, 'r')
    rPr = sub_element(r, 'rPr')
    sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman', cs='Times New Roman')
    sub_element(rPr, 'b')
    sub_element(rPr, 'sz', val='28') # 14pt
    sub_element(rPr, 'szCs', val='28')
    sub_element(rPr, 'color', val='1F497D')
    t = sub_element(r, 't')
    t.text = text
    return p

def make_heading_3(text):
    p = create_element('p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'spacing', before='160', after='60')
    sub_element(pPr, 'jc', val='left')
    
    r = sub_element(p, 'r')
    rPr = sub_element(r, 'rPr')
    sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman', cs='Times New Roman')
    sub_element(rPr, 'b')
    sub_element(rPr, 'i')
    sub_element(rPr, 'sz', val='26') # 13pt
    sub_element(rPr, 'szCs', val='26')
    sub_element(rPr, 'color', val='2E5B82')
    t = sub_element(r, 't')
    t.text = text
    return p

def make_para(text, bold=False, italic=False, align="both", indent=True, space_after='80'):
    p = create_element('p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'spacing', before='40', after=space_after, line='276', lineRule='auto')
    sub_element(pPr, 'jc', val=align)
    if indent:
        sub_element(pPr, 'ind', firstLine='720') # 0.5 inch indent
        
    r = sub_element(p, 'r')
    rPr = sub_element(r, 'rPr')
    sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman', cs='Times New Roman')
    if bold: sub_element(rPr, 'b')
    if italic: sub_element(rPr, 'i')
    sub_element(rPr, 'sz', val='26') # 13pt
    sub_element(rPr, 'szCs', val='26')
    sub_element(rPr, 'color', val='000000')
    t = sub_element(r, 't')
    t.text = text
    return p

def make_rich_para(runs, align="both", indent=True, space_after='80'):
    """runs is a list of tuples: (text, bold, italic, color)"""
    p = create_element('p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'spacing', before='40', after=space_after, line='276', lineRule='auto')
    sub_element(pPr, 'jc', val=align)
    if indent:
        sub_element(pPr, 'ind', firstLine='720')
        
    for r_item in runs:
        text = r_item[0]
        bold = r_item[1] if len(r_item) > 1 else False
        italic = r_item[2] if len(r_item) > 2 else False
        color = r_item[3] if len(r_item) > 3 else '000000'
        
        r = sub_element(p, 'r')
        rPr = sub_element(r, 'rPr')
        sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman', cs='Times New Roman')
        if bold: sub_element(rPr, 'b')
        if italic: sub_element(rPr, 'i')
        sub_element(rPr, 'sz', val='26')
        sub_element(rPr, 'szCs', val='26')
        if color: sub_element(rPr, 'color', val=color)
        t = sub_element(r, 't')
        t.text = text
        if text.startswith(' ') or text.endswith(' '):
            t.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    return p

def make_bullet(bold_prefix, text):
    p = create_element('p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'spacing', before='30', after='40', line='260', lineRule='auto')
    sub_element(pPr, 'ind', left='720', hanging='360')
    sub_element(pPr, 'jc', val='both')
    
    # Bullet icon
    r_dot = sub_element(p, 'r')
    rPr_dot = sub_element(r_dot, 'rPr')
    sub_element(rPr_dot, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
    sub_element(rPr_dot, 'b')
    sub_element(rPr_dot, 'sz', val='26')
    sub_element(rPr_dot, 'color', val='1F497D')
    t_dot = sub_element(r_dot, 't')
    t_dot.text = "• "
    t_dot.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    
    if bold_prefix:
        r_pre = sub_element(p, 'r')
        rPr_pre = sub_element(r_pre, 'rPr')
        sub_element(rPr_pre, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
        sub_element(rPr_pre, 'b')
        sub_element(rPr_pre, 'sz', val='26')
        sub_element(rPr_pre, 'color', val='000000')
        t_pre = sub_element(r_pre, 't')
        t_pre.text = bold_prefix + ": "
        t_pre.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
        
    r_text = sub_element(p, 'r')
    rPr_text = sub_element(r_text, 'rPr')
    sub_element(rPr_text, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
    sub_element(rPr_text, 'sz', val='26')
    sub_element(rPr_text, 'color', val='000000')
    t_text = sub_element(r_text, 't')
    t_text.text = text
    return p

def make_callout(title, text):
    tbl = create_element('tbl')
    tblPr = sub_element(tbl, 'tblPr')
    sub_element(tblPr, 'tblW', w='9500', type='dxa')
    sub_element(tblPr, 'jc', val='center')
    
    tblBorders = sub_element(tblPr, 'tblBorders')
    sub_element(tblBorders, 'top', val='none')
    sub_element(tblBorders, 'left', val='single', sz='24', space='0', color='1F497D') # 3pt left line
    sub_element(tblBorders, 'bottom', val='none')
    sub_element(tblBorders, 'right', val='none')
    
    tblCellMar = sub_element(tblPr, 'tblCellMar')
    sub_element(tblCellMar, 'top', w='120', type='dxa')
    sub_element(tblCellMar, 'bottom', w='120', type='dxa')
    sub_element(tblCellMar, 'left', w='200', type='dxa')
    sub_element(tblCellMar, 'right', w='200', type='dxa')
    
    tr = sub_element(tbl, 'tr')
    tc = sub_element(tr, 'tc')
    tcPr = sub_element(tc, 'tcPr')
    sub_element(tcPr, 'tcW', w='9500', type='dxa')
    sub_element(tcPr, 'shd', val='clear', color='auto', fill='F0F4F8') # soft blue-gray
    
    p = sub_element(tc, 'p')
    pPr = sub_element(p, 'pPr')
    sub_element(pPr, 'spacing', before='60', after='60', line='260', lineRule='auto')
    
    if title:
        r_t = sub_element(p, 'r')
        rPr_t = sub_element(r_t, 'rPr')
        sub_element(rPr_t, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
        sub_element(rPr_t, 'b')
        sub_element(rPr_t, 'sz', val='26')
        sub_element(rPr_t, 'color', val='1F497D')
        t_el = sub_element(r_t, 't')
        t_el.text = "📌 " + title + "\n"
        t_el.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
        
    r_body = sub_element(p, 'r')
    rPr_body = sub_element(r_body, 'rPr')
    sub_element(rPr_body, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
    sub_element(rPr_body, 'i')
    sub_element(rPr_body, 'sz', val='24')
    sub_element(rPr_body, 'color', val='333333')
    t_b = sub_element(r_body, 't')
    t_b.text = text
    return tbl

def make_code_box(code_lines):
    tbl = create_element('tbl')
    tblPr = sub_element(tbl, 'tblPr')
    sub_element(tblPr, 'tblW', w='9500', type='dxa')
    sub_element(tblPr, 'jc', val='center')
    
    tblBorders = sub_element(tblPr, 'tblBorders')
    sub_element(tblBorders, 'top', val='single', sz='6', space='0', color='D3D3D3')
    sub_element(tblBorders, 'left', val='single', sz='20', space='0', color='2E5B82')
    sub_element(tblBorders, 'bottom', val='single', sz='6', space='0', color='D3D3D3')
    sub_element(tblBorders, 'right', val='single', sz='6', space='0', color='D3D3D3')
    
    tblCellMar = sub_element(tblPr, 'tblCellMar')
    sub_element(tblCellMar, 'top', w='100', type='dxa')
    sub_element(tblCellMar, 'bottom', w='100', type='dxa')
    sub_element(tblCellMar, 'left', w='180', type='dxa')
    sub_element(tblCellMar, 'right', w='180', type='dxa')
    
    tr = sub_element(tbl, 'tr')
    tc = sub_element(tr, 'tc')
    tcPr = sub_element(tc, 'tcPr')
    sub_element(tcPr, 'tcW', w='9500', type='dxa')
    sub_element(tcPr, 'shd', val='clear', color='auto', fill='F8F9FA')
    
    for line in code_lines:
        p = sub_element(tc, 'p')
        pPr = sub_element(p, 'pPr')
        sub_element(pPr, 'spacing', before='10', after='10', line='220', lineRule='auto')
        r = sub_element(p, 'r')
        rPr = sub_element(r, 'rPr')
        sub_element(rPr, 'rFonts', ascii='Consolas', hAnsi='Consolas')
        sub_element(rPr, 'sz', val='20') # 10pt
        sub_element(rPr, 'color', val='24292E')
        t = sub_element(r, 't')
        t.text = line
        t.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    return tbl

def make_table(headers, rows, col_widths=None):
    tbl = create_element('tbl')
    tblPr = sub_element(tbl, 'tblPr')
    sub_element(tblPr, 'tblW', w='9500', type='dxa')
    sub_element(tblPr, 'jc', val='center')
    
    tblBorders = sub_element(tblPr, 'tblBorders')
    for b in ['top', 'left', 'bottom', 'right', 'insideH', 'insideV']:
        sub_element(tblBorders, b, val='single', sz='4', space='0', color='BDC3C7')
        
    tblCellMar = sub_element(tblPr, 'tblCellMar')
    sub_element(tblCellMar, 'top', w='120', type='dxa')
    sub_element(tblCellMar, 'bottom', w='120', type='dxa')
    sub_element(tblCellMar, 'left', w='140', type='dxa')
    sub_element(tblCellMar, 'right', w='140', type='dxa')
    
    # Header Row
    tr_h = sub_element(tbl, 'tr')
    trPr_h = sub_element(tr_h, 'trPr')
    sub_element(trPr_h, 'tblHeader')
    
    for idx, h_text in enumerate(headers):
        tc = sub_element(tr_h, 'tc')
        tcPr = sub_element(tc, 'tcPr')
        if col_widths and idx < len(col_widths):
            sub_element(tcPr, 'tcW', w=str(col_widths[idx]), type='dxa')
        sub_element(tcPr, 'shd', val='clear', color='auto', fill='1F497D')
        sub_element(tcPr, 'vAlign', val='center')
        
        p = sub_element(tc, 'p')
        pPr = sub_element(p, 'pPr')
        sub_element(pPr, 'spacing', before='60', after='60')
        sub_element(pPr, 'jc', val='center')
        
        r = sub_element(p, 'r')
        rPr = sub_element(r, 'rPr')
        sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
        sub_element(rPr, 'b')
        sub_element(rPr, 'sz', val='24')
        sub_element(rPr, 'color', val='FFFFFF')
        t = sub_element(r, 't')
        t.text = h_text
        
    # Data Rows
    for r_idx, row in enumerate(rows):
        tr = sub_element(tbl, 'tr')
        bg_color = 'F7F9FB' if r_idx % 2 == 1 else 'FFFFFF'
        
        for c_idx, cell_text in enumerate(row):
            tc = sub_element(tr, 'tc')
            tcPr = sub_element(tc, 'tcPr')
            if col_widths and c_idx < len(col_widths):
                sub_element(tcPr, 'tcW', w=str(col_widths[c_idx]), type='dxa')
            if bg_color != 'FFFFFF':
                sub_element(tcPr, 'shd', val='clear', color='auto', fill=bg_color)
            sub_element(tcPr, 'vAlign', val='center')
            
            p = sub_element(tc, 'p')
            pPr = sub_element(p, 'pPr')
            sub_element(pPr, 'spacing', before='40', after='40')
            # Cột số thứ tự căn giữa, cột còn lại căn trái
            align = 'center' if c_idx == 0 or len(cell_text) <= 5 else 'both'
            sub_element(pPr, 'jc', val=align)
            
            r = sub_element(p, 'r')
            rPr = sub_element(r, 'rPr')
            sub_element(rPr, 'rFonts', ascii='Times New Roman', hAnsi='Times New Roman')
            sub_element(rPr, 'sz', val='24')
            sub_element(rPr, 'color', val='000000')
            t = sub_element(r, 't')
            t.text = cell_text
            
    return tbl

print("OpenXML generators ready!")
