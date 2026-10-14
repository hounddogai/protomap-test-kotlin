package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalEvChargeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalEvChargeTemplateEngine(newRegistry())
        val item = newRegionalEvChargeTemplate("code-690", "label-660")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
