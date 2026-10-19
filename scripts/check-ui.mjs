// Run against the installed DEBUG APK: adb forward tcp:39222 localabstract:webview_devtools_remote_<PID>
// Requires Node 22+. Reads only the local NiakTV page; never reads the TMDB credential.
const targets = await (await fetch("http://127.0.0.1:39222/json/list")).json();
const target = targets.find(
  (t) => t.type === "page" && t.url === "https://niaktv.local/index.html",
);
if (!target) throw new Error("NiakTV debug WebView not found");
const socket = new WebSocket(target.webSocketDebuggerUrl);
let next = 0;
const pending = new Map();
socket.onmessage = (event) => {
  const message = JSON.parse(event.data);
  if (pending.has(message.id)) {
    pending.get(message.id)(message);
    pending.delete(message.id);
  }
};
await new Promise((resolve, reject) => {
  socket.onopen = resolve;
  socket.onerror = reject;
});
function call(method, params) {
  return new Promise((resolve) => {
    const id = ++next;
    pending.set(id, resolve);
    socket.send(JSON.stringify({ id, method, params }));
  });
}
const expression = `(async()=>{
  const assert=(condition,message)=>{if(!condition)throw new Error(message);};
  const rows=['.selection','.inputs-section','.apps-section'].map(q=>document.querySelector(q).getBoundingClientRect());
  assert((document.querySelector('.selection').hidden||rows[0].top<rows[1].top)&&rows[1].top<rows[2].top,'Section order');
  assert(rows[2].bottom<=innerHeight,'Apps dock outside viewport');
  assert(document.fonts.check('12px Manrope')&&document.fonts.check('36px Instrument'),'Custom fonts not ready');
  NiakUI.key('back');let first=document.activeElement;
  assert(first&&!first.closest('[hidden]'),'Initial focus is hidden');
  assert(first.classList.contains('is-focused'),'Remote focus class missing');
  assert(getComputedStyle(first).outlineStyle==='solid','Focus not visible');
  const heading=first.querySelector('.feature-title');
  if(heading){const title=heading.getBoundingClientRect(),button=first.getBoundingClientRect();assert(title.top>=button.top&&title.bottom<button.bottom,'Featured title clipped');}
  assert([...document.querySelectorAll('.app-name')].every(n=>!/[<>]/.test(n.textContent)),'App label contains markup');
  const queue=[...document.querySelectorAll('.resume-card')];
  for(let i=0;i<queue.length;i++)NiakUI.key('right');
  if(queue.length)assert(document.activeElement===queue.at(-1),'Cannot reach all Nuvio programs');
  NiakUI.key('back');
  if(first.dataset.key==='feature'){NiakUI.key('down');assert(document.activeElement.closest('#favorites'),'Down must reach favorites');NiakUI.key('down');assert(document.activeElement.closest('#inputs'),'Down must reach inputs');}
  if(document.activeElement.closest('#inputs')){NiakUI.key('down');assert(document.activeElement.closest('#apps'),'Down must reach app dock');}
  NiakUI.key('menu');assert(!document.getElementById('overlay').hidden,'Settings drawer did not open');
  NiakUI.key('back');assert(document.getElementById('overlay').hidden,'Back did not close drawer');
  // Capture a fresh native model without reading private Android preferences.
  const update=NiakUI.update;
  const original=await new Promise((resolve,reject)=>{
    const timer=setTimeout(()=>{NiakUI.update=update;reject(new Error('Native model timeout'));},10000);
    NiakUI.update=value=>{clearTimeout(timer);NiakUI.update=update;update(value);resolve(value);};
    Device.action('refresh','');
  });
  try {
    const netflix={id:'com.netflix.ninja',name:'Netflix',icon:''};
    const fixture={...original,apps:[netflix],resumes:[],favorites:[netflix.id],order:[],defaultStreamingApp:netflix.id,nuvioInstalled:false,resumePermission:false,language:'en',showSelection:true,performance:false,background:'gradient',palette:'violet'};
    NiakUI.update({...fixture,background:'cinema',wallpaperTitle:'Original film'});
    assert(document.getElementById('wallpaper-credit').textContent==='Wallpaper · Original film · TMDB','Original title credit order');
    NiakUI.update(fixture);NiakUI.key('back');
    assert(document.documentElement.lang==='en','English language not applied');
    assert(document.querySelector('h1').textContent.includes('My selection'),'Selection heading not translated');
    assert(document.querySelector('#continue [data-action=app]').dataset.id===netflix.id,'Installed streaming fallback missing');
    assert(!document.querySelector('#continue').textContent.includes('Nuvio'),'Nuvio leaked into non-Nuvio hero');
    NiakUI.key('menu');assert(!document.querySelector('#drawer-content [data-action=permission]'),'Nuvio permission offered without Nuvio');
    assert(document.getElementById('drawer-title').textContent==='Settings','Settings not translated');
    NiakUI.key('back');
    NiakUI.update({...fixture,apps:[],favorites:[],defaultStreamingApp:''});
    assert(!document.querySelector('#continue button'),'Invented fallback when no streaming app exists');
    NiakUI.update({...fixture,showSelection:false,performance:true,background:'solid'});NiakUI.key('back');
    assert(document.querySelector('.selection').hidden,'Selection should be hidden');
    assert(document.activeElement.closest('#inputs'),'Hidden content still receives focus');
    NiakUI.key('down');assert(document.activeElement.closest('#apps'),'Cannot navigate minimal layout');
    assert(document.querySelector('.apps-section').getBoundingClientRect().bottom<=innerHeight,'Minimal dock outside viewport');
    assert(document.getElementById('wallpaper').style.backgroundImage==='none','Cinema image retained in solid mode');
    NiakUI.update({...fixture,language:'fr'});
    assert(document.querySelector('h1').textContent.includes('Ma sélection'),'French restore failed');
  } finally { NiakUI.key('back');NiakUI.update(original);NiakUI.key('back'); }
  return {passed:true,optionalAppAndLanguageCases:true,resumeCards:document.querySelectorAll('[data-action="resume"]').length,apps:document.querySelectorAll('.app').length,viewport:[innerWidth,innerHeight]};
})()`;
const result = await call("Runtime.evaluate", {
  expression,
  returnByValue: true,
  awaitPromise: true,
});
socket.close();
if (result.result?.exceptionDetails)
  throw new Error(
    result.result.exceptionDetails.exception?.description ||
      "UI assertion failed",
  );
console.log(JSON.stringify(result.result?.result?.value));
