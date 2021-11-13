// Functions used in EventDoc
// Right now, only toggle().

function toggle(obj) {
	var el = document.getElementById(obj);
	if ( el.style.display != 'none' ) {
		el.style.display = 'none';
	} else {
		el.style.display = '';
	}
}