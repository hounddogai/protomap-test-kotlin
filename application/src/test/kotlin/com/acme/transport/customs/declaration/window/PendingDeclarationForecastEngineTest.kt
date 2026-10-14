package com.acme.transport.customs.declaration.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDeclarationForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDeclarationForecastEngine(newRegistry())
        val item = newPendingDeclarationForecast("code-269", "title-655")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
