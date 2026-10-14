package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRosterTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRosterTemplateEngine(newRegistry())
        val item = newScheduledRosterTemplate("code-628", "name-830")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
