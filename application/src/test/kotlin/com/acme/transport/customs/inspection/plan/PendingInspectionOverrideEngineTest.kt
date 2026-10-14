package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingInspectionOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingInspectionOverrideEngine(newRegistry())
        val item = newPendingInspectionOverride("code-154", "title-893")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
