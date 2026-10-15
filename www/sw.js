const VERSION='fit2x-v19-food-images';
const VIDEOS=["./videos/band-assisted-pull-up.mp4","./videos/barbell-back-squat.mp4","./videos/barbell-bench-press.mp4","./videos/bodyweight-squat.mp4","./videos/burpee.mp4","./videos/cable-kneeling-crunch.mp4","./videos/cable-rear-delt-row-with-rope.mp4","./videos/cable-rope-overhead-triceps-extension.mp4","./videos/cable-rope-triceps-pushdown.mp4","./videos/cable-standing-fly.mp4","./videos/dumbbell-bent-over-row.mp4","./videos/dumbbell-biceps-curl.mp4","./videos/dumbbell-cross-body-hammer-curl.mp4","./videos/dumbbell-incline-bench-press.mp4","./videos/dumbbell-incline-curl.mp4","./videos/dumbbell-lateral-raise.mp4","./videos/dumbbell-stiff-leg-deadlift.mp4","./videos/elliptical-trainer.mp4","./videos/ez-bar-biceps-curl.mp4","./videos/front-plank.mp4","./videos/glute-bridge.mp4","./videos/hanging-leg-hip-raise.mp4","./videos/hip-circles-stretch.mp4","./videos/incline-push-up.mp4","./videos/jumping-jack.mp4","./videos/lever-horizontal-leg-press.mp4","./videos/lever-leg-extension.mp4","./videos/lever-lying-chest-press.mp4","./videos/lever-lying-leg-curl.mp4","./videos/lever-seated-reverse-fly.mp4","./videos/lever-standing-calf-raise.mp4","./videos/pec-deck-fly.mp4","./videos/seated-cable-row-v-grip.mp4","./videos/seated-dumbbell-shoulder-press.mp4","./videos/wide-grip-cable-lat-pulldown.mp4"];
const FOOD_IMAGES=["./food-images/banana.jpg","./food-images/biryani.jpg","./food-images/bread.jpg","./food-images/chai.jpg","./food-images/chicken-curry.jpg","./food-images/chicken.jpg","./food-images/coffee.jpg","./food-images/dal.jpg","./food-images/dosa.jpg","./food-images/egg.jpg","./food-images/fish.jpg","./food-images/idli.jpg","./food-images/lassi.jpg","./food-images/milk.jpg","./food-images/noodles.jpg","./food-images/nuts.jpg","./food-images/oats.jpg","./food-images/paneer.jpg","./food-images/peanut-butter.jpg","./food-images/pizza.jpg","./food-images/rice.jpg","./food-images/roti.jpg","./food-images/salad.jpg","./food-images/samosa.jpg","./food-images/sweets.jpg"];
const CORE=['./','./index.html','./manifest.webmanifest','./app-icon-192.png','./app-icon-512.png','./firebase-config.json','./food-database.json',...VIDEOS,...FOOD_IMAGES];
self.addEventListener('install',event=>event.waitUntil(caches.open(VERSION).then(cache=>Promise.all(CORE.map(url=>fetch(url).then(response=>{if(!response.ok)throw new Error(`${response.status} ${url}`);return cache.put(url,response)}).catch(error=>console.warn('FIT2X cache skipped',url,error.message))))).then(()=>self.skipWaiting())));
self.addEventListener('activate',event=>event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(key=>key!==VERSION).map(key=>caches.delete(key)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',event=>{
  if(event.request.method!=='GET')return;
  event.respondWith(fetch(event.request).then(response=>{const copy=response.clone();caches.open(VERSION).then(cache=>cache.put(event.request,copy)).catch(()=>{});return response}).catch(()=>caches.match(event.request).then(hit=>hit||(event.request.mode==='navigate'?caches.match('./index.html'):Response.error()))));
});
self.addEventListener('push',event=>{
  let data={title:'FIT2X reminder',body:'Open FIT2X to check today’s plan.'};
  try{if(event.data)data={...data,...event.data.json()}}catch{if(event.data)data.body=event.data.text()||data.body}
  event.waitUntil(self.registration.showNotification(data.title,{body:data.body,icon:'./app-icon-192.png',badge:'./app-icon-192.png',tag:data.tag||'fit2x-push',data}));
});
self.addEventListener('notificationclick',event=>{
  event.notification.close();
  event.waitUntil(clients.matchAll({type:'window',includeUncontrolled:true}).then(list=>{
    for(const client of list){if('focus' in client)return client.focus()}
    const target=event.notification.data?.url||'./';return clients.openWindow(target);
  }));
});
