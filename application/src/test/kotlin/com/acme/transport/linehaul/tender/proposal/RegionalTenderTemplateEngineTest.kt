package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTenderTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTenderTemplateEngine(newRegistry())
        val item = newRegionalTenderTemplate("code-654", "title-22")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
