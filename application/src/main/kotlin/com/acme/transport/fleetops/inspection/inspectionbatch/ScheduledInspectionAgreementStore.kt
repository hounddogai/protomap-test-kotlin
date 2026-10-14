package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled inspection agreements by code. */
interface ScheduledInspectionAgreementStore {
    fun find(code: String): ScheduledInspectionAgreement?
    fun save(item: ScheduledInspectionAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledInspectionAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledInspectionAgreementStore(
    var items: MutableMap<String, ScheduledInspectionAgreement>,
    var order: MutableList<String>,
) : ScheduledInspectionAgreementStore {
    override fun find(code: String): ScheduledInspectionAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledInspectionAgreement) {
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

    override fun list(): MutableList<ScheduledInspectionAgreement> {
        val out = mutableListOf<ScheduledInspectionAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledInspectionAgreementStore(): ScheduledInspectionAgreementStore {
    return MemoryScheduledInspectionAgreementStore(
        mutableMapOf<String, ScheduledInspectionAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledInspectionAgreementStore(
    var inner: ScheduledInspectionAgreementStore,
    var cache: TtlCache<ScheduledInspectionAgreement>,
    var hits: Int,
    var misses: Int,
) : ScheduledInspectionAgreementStore {
    override fun find(code: String): ScheduledInspectionAgreement? {
        val hit = (cache.get(code) as ScheduledInspectionAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledInspectionAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledInspectionAgreement> = inner.list()
}

fun newCachedScheduledInspectionAgreementStore(inner: ScheduledInspectionAgreementStore, ttl: Int, clock: Clock): ScheduledInspectionAgreementStore {
    return CachedScheduledInspectionAgreementStore(
        inner = inner,
        cache = newTtlCache<ScheduledInspectionAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled inspection agreements. Empty fields match all. */
data class ScheduledInspectionAgreementQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledInspectionAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledInspectionAgreement>): MutableList<ScheduledInspectionAgreement> {
        val matched = filter(items) { candidate: ScheduledInspectionAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledInspectionAgreements(store: ScheduledInspectionAgreementStore, query: ScheduledInspectionAgreementQuery): MutableList<ScheduledInspectionAgreement> {
    return query.apply(store.list())
}
