s.options.numBuffers = 16000;
s.options.memSize = 655360;
s.boot;
s.freqscope;
s.plotTree;
s.scope;

t = PdefAllGui(8);

TempoClock.default.tempo();
// 80 BPM
TempoClock.default.tempo_(80/60);


// G, A, B, C, D, E, and F♯

//Scale.major.degreeToFreq((0..7), (60-5).midicps, 0);
~notes = Scale.major.degreeToFreq((0..(8*7)), 31.midicps, 0);
~gs = Scale.major.degreeToFreq((0..8)*7, 31.midicps, 0);
~gsm = ~gs.cpsmidi;
~gsmroot = ~gsm - 60;

//
//    I – G major, G major seventh (Gmaj, Gmaj7)
//    ii – A minor, A minor seventh (Am, Am7)
//    iii – B minor, B minor seventh (Bm, Bm7)
//    IV – C major, C major seventh (C, Cmaj 7)
//    V – D major, D dominant seventh (D, D7)
//    vi – E minor, E minor seventh (Em, Em7)
//    vii – F# diminished, F# minor seventh flat five (F#°, F#m7b5)
// Common chord progressions in G major
// I - IV - V 	    G - C - D
// I - vi - IV - V 	G - Em - C - D
// ii - V - I 	    Am - D7 - GM7
~gmaj   = [0, 2, 4]; //G B D
~gmaj7  = [0, 2, 4, 6]; //G B D F#
~amin   = [1,3,5]; // A C E
~bmin   = [2,4,6];//B D F#
~bmin7  = [2,4,6,8];//B D F# A
~cmaj   = [-4,-2,0]; //C E G
~cmaj7  = [-4,-2,0,2]; //C E G
~dmaj   = [-3,-1,1];//DF#A
~dmaj7  = [-3,-1,1,3];//DF#AC
~emin   = [-2,0,2];//EGB
~emin7  = [-2,0,2,4];//EGBD
~fsdim  = [-1,1,3];//F#AC
~fsdim7 = [-1,1,3,5];//F#ACE

~chords = [~gmaj,~amin,~bmin,~cmaj,~dmaj,~emin,~fsdim];
~chords7 = [~gmaj7,~bmin7,~cmaj7,~dmaj7,~emin7,~fsdim7];
~allchords = [~chords,~chords7].lace(); // zip

// I - vi - IV - V 	G - Em - C - D
// ii - V - I 	    Am - D7 - GM7
//
~progI = [~gmaj,~cmaj,~dmaj];
~progII = [~gmaj,~emin,~cmaj,~dmaj];




SynthDef(\drone, { |out, freq = 440, gate = 0.5, amp = 1.0, attack = 0.04, release=0.1 |
	var sig,nsize,n = (2..20);
	nsize = n.size;
	sig = ((
		n.collect {arg i; 
			SinOsc.ar( (1.0 - (1.0/(i*i))) * freq )
		}).sum / nsize)
	* EnvGen.kr(Env.adsr(attack, 0.2, 0.6, release), gate, doneAction:2)
	* amp;
    Out.ar(out, sig ! 2)
}).add;

	Pbind(
		\instrument,\drone,
		\dur, Pstutter(4,Pshuf([0.1,0.2,0.4,0.5,1.0,4.0],inf),inf),
		\degree, Pshuf([Pseq(~progI),Pseq(~progII)], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, Pstutter(4,Pshuf(~gsmroot[(0..(~gsmroot.size / 2))],inf),inf), // semitones relative to 60.midicps, so this is G
//\attack, 0.0,
//		\release, 1.0
	).play;

Pdef(\x, Pbind(\instrument,\drone,
	\dur, Pstutter(4,Pshuf([1.0,2.0,0.1,0.2],inf),inf),
	\degree, Pshuf([Pseq(~progI),Pseq(~progII)], inf), // your melody goes here
	\scale, Scale.major, // your scale goes here
	\attack, 0.3,
	\release, 3.0,
	\root, Pstutter(4,Pshuf(~gsmroot[(0..(3 * ~gsmroot.size / 4))],inf),inf) //
));
Pdef(\x).quant = 2.0; // 2 seconds
Pbindf(Pdef(\x)).play;

Pdef(\x, Pbind(\instrument,\drone,
	\dur, Pstutter(4,Pshuf([1.0,2.0,0.1,0.2],inf),inf),
	\degree, Pshuf(~chords, inf), // your melody goes here
	\scale, Scale.major, // your scale goes here
	\attack, 0.3,
	\release, Prand([3.0,1.0,5.0,0.1],inf),
	\root, Pstutter(~chords.size,Pshuf(~gsmroot[(0..(3 * ~gsmroot.size / 4))],inf),inf) //
));

// lots of drones together
(
a = 	Pbind(\instrument,\drone,
	\dur, Pstutter(2,Pshuf([0.1,0.2,0.3,0.4,0.5],inf),inf),
	\degree, Prand(~allchords, inf), // your melody goes here
	\scale, Scale.major, // your scale goes here
	\attack, Prand([0.3,0.5,0.1],inf),
	\release, Prand([3.0,1.0,5.0,0.1],inf),
	\root, Pstutter(4,Pshuf(~gsmroot[(0..(4 * ~gsmroot.size / 4))],inf),inf)
);
b = Pbind(\instrument,\drone,
	\dur, Pshuf([5.0,4.0,3.0,9.0],inf),
	\degree, Prand(~chords ++ (\rest!1),inf),
	\scale, Scale.major,
	\attack, 0.3,
	\release, Prand([3.0,1.0],inf),
    //	\detune, Prand([0.0,0.0,0.0,1.0,3.0,30.0],inf),
	\root, Pstutter(4,Pshuf(~gsmroot[(0..(3 * ~gsmroot.size / 4))],inf),inf) //
);
Pdef(\x,Ppar([a,b,a,b,a,b]));
)
Pdef(\x, Pbind(\instrument,\drone,
	\dur, Pstutter(4,Pshuf([1/8,1/4,1,2,1/16],inf),inf),
	\degree, Pshuf(~allchords, inf), // your melody goes here
	\scale, Scale.major, // your scale goes here
	\attack, Prand([0.3,0.1,0.05,0.5],inf),
	\release, Prand([3.0,1.0,5.0,0.1],inf),
	\root, Pstutter(~chords.size,Pshuf(~gsmroot[(0..(3 * ~gsmroot.size / 4))],inf),inf) //
));
Pdef(\x).play;
Pbindef(\x, \dur, Prand([1/2,1,2,4,8],inf)).play;
Pbindef(\x, \dur, Pstutter(~chords.size,Pshuf([1/8,1/4,1,2,1/16,4],inf),inf)).play;
Pbindef(\x, \degree, Pshuf((0..7), inf)).play;

// Play low
Pbindef(\x,	\root, Pstutter(~chords.size,Pshuf(~gsmroot[(0..(1 * ~gsmroot.size / 4))],inf),inf)).play; //
Pbindef(
// Play higher
Pbindef(\x,	\root, Pstutter(~chords.size,Pshuf(~gsmroot[((~gsmroot.size / 8)..(~gsmroot.size - 1))],inf),inf)).play; //
	
Pbindef(\x,\degree, Pshuf([Pseq(~progI),Pseq(~progII)], inf)).play;

// see your pdefs




(
a = Pdef(\a,Pbind(\instrument,\drone,
	\dur, Pstutter(2,Pshuf([0.1,0.2,0.3,0.4,0.5],inf),inf),
	\degree, Prand(~allchords, inf), // your melody goes here
	\scale, Scale.major, // your scale goes here
	\attack, Prand([0.3,0.5,0.1],inf),
	\release, Prand([3.0,1.0,5.0,0.1],inf),
	\root, Pstutter(4,Pshuf(~gsmroot[(0..(4 * ~gsmroot.size / 4))],inf),inf)
));
b = Pdef(\b,Pbind(\instrument,\drone,
	\dur, Pshuf([5.0,4.0,3.0,9.0],inf),
	\degree, Prand(~chords ++ (\rest!1),inf),
	\scale, Scale.major,
	\attack, 0.3,
	\release, Prand([3.0,1.0],inf),
    //	\detune, Prand([0.0,0.0,0.0,1.0,3.0,30.0],inf),
	\root, Pstutter(4,Pshuf(~gsmroot[(0..(3 * ~gsmroot.size / 4))],inf),inf) //
));
Pdef(\y,Ppar([Pdef(\a),Pdef(\b)]));
)
Pdef(\y).quant = 2.0;
Pdef(\y).play;
Pdef(\y).stop;
Pdef(\x).stop;
Pbindef(\a,\dur,1/2).play;
Pbindef(\b,\dur,1/2).play;
Pbindef(\b, \dur, Pshuf([5.0,4.0,3.0,9.0],inf)).play;
Pbindef(\b, \degree, Pshuf(~allchords,inf)).play;
Pbindef(\b, \root, ~gsmroot[0]).play;
Pbindef(\b,	\root, Pstutter(7,Pshuf(~gsmroot,inf),inf)); //

Pbindef(\a, \root, ~gsmroot[0]-24).play;
Pbindef(\a, \dur, Pstutter(2,Pshuf([1/2,1/2,3/4,1],inf),inf)).play;

/* 
~mystupidpbinds = {

	Pbind(
		\degree, Pseq((-14..14), inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, -5 // semitones relative to 60.midicps, so this is G
	).play;
	Pbind(
		\degree, Pseq((14..-14), inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, -5 // semitones relative to 60.midicps, so this is G
	).play;
	
	
	Pbind(\freq,Pseq(~notes, inf)).play;
	// I want all the chords
	
	// I - IV - V 	    G - C - D
	Pbind(
		\degree, Pseq([~gmaj,~cmaj,~dmaj,~gmaj7,~cmaj7,~dmaj7], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, -5 // semitones relative to 60.midicps, so this is G
	).play;
	// I - vi - IV - V 	G - Em - C - D
// ii - V - I 	    Am - D7 - GM7
	Pbind(
		\dur, Prand([0.44, 0.33], inf),
		\degree, Pseq([~gmaj,~emin,~cmaj,~dmaj], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, Pstutter(4,Pshuf(~gsmroot,inf),inf), // semitones relative to 60.midicps, so this is G
	).play;

	Pbind(
		\instrument, \test,
		\dur, Prand([0.1, 0.3, 1.0, 2.0,6.0,0.2], inf),
		\degree, Pseq([~amin,~dmaj7,~gmaj7], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, -1*(5+12) // semitones relative to 60.midicps, so this is G
	).play;
	// the SynthDef
	SynthDef(\test, { | out, freq = 440, amp = 0.1, nharms = 10, pan = 0, gate = 1 |
		var audio = Blip.ar(freq, nharms, amp);
		var env = Linen.kr(gate, doneAction: 2);
		OffsetOut.ar(out, Pan2.ar(audio, pan, env) );
	}).add;
	

	
	Pbind(
		\dur, Prand([0.44, 0.33], inf),
		\degree, Pshuf([Pseq(~progI,4),Pseq(~progII,4)], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, Pstutter(4,Pshuf(~gsmroot,inf),inf), // semitones relative to 60.midicps, so this is G
	).play;
)	
};
*/



SynthDef(\sawpulse, { |out, freq = 440, gate = 0.5, plfofreq = 6, mw = 0, ffreq = 2000, rq = 0.3, freqlag = 0.05, amp = 1|
    var sig, plfo, fcurve;
    plfo = SinOsc.kr(plfofreq, mul:mw, add:1);
    freq = Lag.kr(freq, freqlag) * plfo;
    fcurve = EnvGen.kr(Env.adsr(0, 0.3, 0.1, 20), gate);
    fcurve = (fcurve - 1).madd(0.7, 1) * ffreq;
    sig = Mix.ar([Pulse.ar(freq, 0.9), Saw.ar(freq*1.007)]);
    sig = RLPF.ar(sig, fcurve, rq)
        * EnvGen.kr(Env.adsr(0.04, 0.2, 0.6, 0.1), gate, doneAction:2)
        * amp;
    Out.ar(out, sig ! 2)
}).add;
Pdef(\saw,
	Pbind(
		\instrument,\sawpulse,
		\dur, Pstutter(4,Pshuf([1/16,1/4,1/2],inf),inf),
		\degree, Pshuf([Pseq(~progI),Pseq(~progII)], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, Pstutter(4,Pshuf(~gsmroot[(0..(~gsmroot.size-2))],inf),inf), // semitones relative to 60.midicps, so this is G
		\attack, 0.0,
		\release, 1.0
	));

Pdef(\saw).play;

Pdef(\saw).quant = 0.0;
Pbindef(\saw, \dur, Pseq([1/4,1/8,1/8,1/8],inf)).play;
Pbindef(\saw, \attack, 0.1).play;
Pbindef(\saw, \release, 0.1).play;
Pbindef(\saw, \dur, Pstutter(4,Pshuf([1,2,3,4,8],inf),inf),).play;
Pbindef(\saw, \root, Pstutter(4,Prand(~gsmroot[(0..(~gsmroot.size-2))],inf),inf)).play;
Pbindef(\saw, \amp, 0.1).play;
Pbindef(\saw,\degree, Pstutter(4,Pshuf(~allchords ++ (\rest!3), inf))).play; // your melody goes h

// Here's a fade out of notes
Pbindef(\saw, \amp, Pseq((0.1*(0..100)/100.0).reverse)).play;

Pbindef(\a, \amp, Pseq((0.1*(0..100)/100.0).reverse)).play;
Pbindef(\b, \amp, Pseq((0.1*(0..100)/100.0).reverse)).play;
Pbindef(\y, \amp, Pseq((0.1*(0..100)/100.0).reverse)).play;
Pbindef(\y, \midinote, Pseq((32..64).reverse)).play;