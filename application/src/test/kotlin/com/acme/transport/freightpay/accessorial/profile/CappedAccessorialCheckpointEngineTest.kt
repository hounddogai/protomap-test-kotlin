package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAccessorialCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAccessorialCheckpointEngine()
        val item = newCappedAccessorialCheckpoint("code-590", "name-747")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
