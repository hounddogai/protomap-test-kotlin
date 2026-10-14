package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification slots by code. */
interface QualificationSlotStore {
    fun find(code: String): QualificationSlot?
    fun save(item: QualificationSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationSlotStore(
    var items: MutableMap<String, QualificationSlot>,
    var order: MutableList<String>,
) : QualificationSlotStore {
    override fun find(code: String): QualificationSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationSlot) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<QualificationSlot> {
        val out = mutableListOf<QualificationSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationSlotStore(): QualificationSlotStore {
    return MemoryQualificationSlotStore(mutableMapOf<String, QualificationSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationSlotStore(
    var inner: QualificationSlotStore,
    var cache: TtlCache<QualificationSlot>,
    var hits: Int,
    var misses: Int,
) : QualificationSlotStore {
    override fun find(code: String): QualificationSlot? {
        val hit = (cache.get(code) as QualificationSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationSlot> = inner.list()
}

fun newCachedQualificationSlotStore(inner: QualificationSlotStore, ttl: Int, clock: Clock): QualificationSlotStore {
    return CachedQualificationSlotStore(
        inner = inner,
        cache = newTtlCache<QualificationSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification slots. Empty fields match all. */
data class QualificationSlotQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationSlot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationSlot>): MutableList<QualificationSlot> {
        val matched = filter(items) { candidate: QualificationSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationSlots(store: QualificationSlotStore, query: QualificationSlotQuery): MutableList<QualificationSlot> {
    return query.apply(store.list())
}
