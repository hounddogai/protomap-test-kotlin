package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIdlingExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIdlingExceptionEngine(newRegistry())
        val item = newRegionalIdlingException("code-913", "title-399")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
