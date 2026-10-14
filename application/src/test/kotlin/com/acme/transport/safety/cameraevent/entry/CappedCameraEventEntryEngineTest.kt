package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCameraEventEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCameraEventEntryEngine()
        val item = newCappedCameraEventEntry("code-448", "name-852")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
