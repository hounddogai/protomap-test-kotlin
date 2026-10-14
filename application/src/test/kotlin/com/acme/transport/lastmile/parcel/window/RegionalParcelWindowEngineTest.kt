package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalParcelWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalParcelWindowEngine(newRegistry())
        val item = newRegionalParcelWindow("code-771", "title-412")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
