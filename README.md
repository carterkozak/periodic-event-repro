# JDK-21 JFR Periodic Events Missing

The first recording within the process seems to work as expected, however subsequent recordings do not produce periodic events if there's a sufficient pause between recordings.

The resulting JFR recording does not contain periodic events, nor are they picked up by the EventStream or RecordingStream.

This reproducer succeeds using Java 17, but fails on Java 21.

If I do not add a sleep between invocations of `Main.operation`, this code
no longer reproduces the problem. I'm suspicious that periodic event code may get into an error state if it executes without an active recording.
