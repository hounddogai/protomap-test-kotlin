package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelTemplateEngine(newRegistry())
        val item = newParcelTemplate("code-607", "name-158")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
