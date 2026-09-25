"""Download the fixed demonstration image set; origins also live beside each asset."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import json, urllib.request

root = Path(__file__).resolve().parents[1]
assets = root / 'app/src/main/assets/products'
photos = {
    'headphones': 'photo-1546435770-a3e426bf472b',
    'earbuds': 'photo-1606220945770-b5b6c2c55bf1',
    'watch': 'photo-1523275335684-37898b6baf30',
    'sneakers': 'photo-1549298916-b41d501d3772',
    'hoodie': 'photo-1556821840-3a63f95609a7',
    'backpack': 'photo-1553062407-98eeb64c6a62',
    'perfume': 'photo-1541643600914-78b084683601',
    'mouse': 'photo-1527814050087-3793815479db',
    'keyboard': 'photo-1587829741301-dc798b83add3',
    'speaker': 'photo-1608043152269-423dbba4e7e1',
    'tshirt': 'photo-1521572163474-6864f9cf17ab',
    'lamp': 'photo-1507473885765-e6ed057f782c',
    'bottle': 'photo-1602143407151-7111542de6e8',
    'running': 'photo-1542291026-7eec264c27ff',
    'serum': 'photo-1608571423902-eed4a5ad8108',
    'classicwatch': 'photo-1524805444758-089113d48a6d',
}
urls = {key: f'https://images.unsplash.com/{value}?auto=format&fit=crop&w=900&q=85' for key,value in photos.items()}
catalog = json.loads((root / 'docs/asset-catalogue.json').read_text(encoding='utf-8-sig'))
for key, pid in [('powerbank',105),('phonecase',108)]:
    urls[key] = next(p['thumbnail'] for p in catalog if p['id']==pid)

def download(entry):
    key,url = entry
    target = assets / f'{key}.jpg'
    req = urllib.request.Request(url, headers={'User-Agent':'BluntPrototype/1.0'})
    with urllib.request.urlopen(req,timeout=50) as response:
        content = response.read()
    target.write_bytes(content)
    (assets / f'{key}.jpg.origin.json').write_text(json.dumps({'origin':url,'usage':'Illustrative local demo photography, not a product identity claim'},indent=2))
    return key,len(content)
with ThreadPoolExecutor(max_workers=6) as pool:
    for result in pool.map(download, urls.items()): print(result)
(root/'docs/ASSET-SOURCES.md').write_text('# Demonstration photography\n\nImages are illustrative. Product names, reviews, prices and specifications are synthetic catalogue data; photography is not proof of model, brand or color. Replace with licensed exact SKU images before publication.\n\n'+'\n'.join(f'- {name}: {url}' for name,url in urls.items()), encoding='utf-8')
