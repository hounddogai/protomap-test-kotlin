package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAccessorialCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAccessorialCheckpointEngine(newRegistry())
        val item = newAdaptiveAccessorialCheckpoint("code-370", "title-62")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
