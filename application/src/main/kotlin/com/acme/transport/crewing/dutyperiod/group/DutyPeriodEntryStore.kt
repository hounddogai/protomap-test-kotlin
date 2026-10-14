package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period entries by code. */
interface DutyPeriodEntryStore {
    fun find(code: String): DutyPeriodEntry?
    fun save(item: DutyPeriodEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodEntryStore(
    var items: MutableMap<String, DutyPeriodEntry>,
    var order: MutableList<String>,
) : DutyPeriodEntryStore {
    override fun find(code: String): DutyPeriodEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodEntry) {
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

    override fun list(): MutableList<DutyPeriodEntry> {
        val out = mutableListOf<DutyPeriodEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodEntryStore(): DutyPeriodEntryStore {
    return MemoryDutyPeriodEntryStore(mutableMapOf<String, DutyPeriodEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodEntryStore(
    var inner: DutyPeriodEntryStore,
    var cache: TtlCache<DutyPeriodEntry>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodEntryStore {
    override fun find(code: String): DutyPeriodEntry? {
        val hit = (cache.get(code) as DutyPeriodEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodEntry> = inner.list()
}

fun newCachedDutyPeriodEntryStore(inner: DutyPeriodEntryStore, ttl: Int, clock: Clock): DutyPeriodEntryStore {
    return CachedDutyPeriodEntryStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period entries. Empty fields match all. */
data class DutyPeriodEntryQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodEntry>): MutableList<DutyPeriodEntry> {
        val matched = filter(items) { candidate: DutyPeriodEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodEntries(store: DutyPeriodEntryStore, query: DutyPeriodEntryQuery): MutableList<DutyPeriodEntry> {
    return query.apply(store.list())
}
