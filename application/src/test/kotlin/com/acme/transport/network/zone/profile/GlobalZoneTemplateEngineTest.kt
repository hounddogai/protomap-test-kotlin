package com.acme.transport.network.zone.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalZoneTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalZoneTemplateEngine(newRegistry())
        val item = newGlobalZoneTemplate("code-738", "title-234")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
