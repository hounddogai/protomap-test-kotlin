package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDeclarationForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDeclarationForecastEngine(newRegistry())
        val item = newPrimaryDeclarationForecast("code-353", "name-16")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
