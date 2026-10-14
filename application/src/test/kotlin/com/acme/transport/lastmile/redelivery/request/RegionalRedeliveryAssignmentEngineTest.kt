package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRedeliveryAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRedeliveryAssignmentEngine(newRegistry())
        val item = newRegionalRedeliveryAssignment("code-660", "name-971")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
