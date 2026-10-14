package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalBackhaulBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalBackhaulBaselineEngine(newRegistry())
        val item = newRegionalBackhaulBaseline("code-682", "title-135")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
