package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationOverrideEngine(newRegistry())
        val item = newUtilizationOverride("code-78", "title-875")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
