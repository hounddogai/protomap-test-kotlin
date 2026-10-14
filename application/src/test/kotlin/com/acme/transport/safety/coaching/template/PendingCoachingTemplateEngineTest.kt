package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCoachingTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCoachingTemplateEngine(newRegistry())
        val item = newPendingCoachingTemplate("code-987", "label-346")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
