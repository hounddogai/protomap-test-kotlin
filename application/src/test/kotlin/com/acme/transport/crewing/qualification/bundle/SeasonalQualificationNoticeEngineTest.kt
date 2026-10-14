package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalQualificationNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalQualificationNoticeEngine(newRegistry())
        val item = newSeasonalQualificationNotice("code-743", "label-472")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
