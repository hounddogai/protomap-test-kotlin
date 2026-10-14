package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCrossDockSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCrossDockSnapshotEngine(newRegistry())
        val item = newDefaultCrossDockSnapshot("code-367", "name-91")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
