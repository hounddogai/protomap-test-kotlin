package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTireRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTireRequestEngine(newRegistry())
        val item = newPrimaryTireRequest("code-657", "title-171")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
