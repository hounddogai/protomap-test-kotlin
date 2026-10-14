package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanBundleEngine(newRegistry())
        val item = newLanePlanBundle("code-480", "name-168")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
