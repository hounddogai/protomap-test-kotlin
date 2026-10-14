package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultParcelTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultParcelTierEngine(newRegistry())
        val item = newDefaultParcelTier("code-222", "label-668")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
