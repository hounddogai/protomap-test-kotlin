package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCameraEventEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCameraEventEntryEngine(newRegistry())
        val item = newDefaultCameraEventEntry("code-485", "label-530")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
