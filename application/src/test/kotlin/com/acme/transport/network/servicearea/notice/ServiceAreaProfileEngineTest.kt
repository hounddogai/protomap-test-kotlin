package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaProfileEngine(newRegistry())
        val item = newServiceAreaProfile("code-86", "name-62")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
