package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingInspectionOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingInspectionOverrideEngine(newRegistry())
        val item = newPendingInspectionOverride("code-55", "title-489")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
