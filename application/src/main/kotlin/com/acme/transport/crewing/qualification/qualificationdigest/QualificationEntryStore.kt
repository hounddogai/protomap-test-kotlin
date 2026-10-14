package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification entries by code. */
interface QualificationEntryStore {
    fun find(code: String): QualificationEntry?
    fun save(item: QualificationEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationEntryStore(
    var items: MutableMap<String, QualificationEntry>,
    var order: MutableList<String>,
) : QualificationEntryStore {
    override fun find(code: String): QualificationEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationEntry) {
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

    override fun list(): MutableList<QualificationEntry> {
        val out = mutableListOf<QualificationEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationEntryStore(): QualificationEntryStore {
    return MemoryQualificationEntryStore(mutableMapOf<String, QualificationEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationEntryStore(
    var inner: QualificationEntryStore,
    var cache: TtlCache<QualificationEntry>,
    var hits: Int,
    var misses: Int,
) : QualificationEntryStore {
    override fun find(code: String): QualificationEntry? {
        val hit = (cache.get(code) as QualificationEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationEntry> = inner.list()
}

fun newCachedQualificationEntryStore(inner: QualificationEntryStore, ttl: Int, clock: Clock): QualificationEntryStore {
    return CachedQualificationEntryStore(
        inner = inner,
        cache = newTtlCache<QualificationEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification entries. Empty fields match all. */
data class QualificationEntryQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationEntry>): MutableList<QualificationEntry> {
        val matched = filter(items) { candidate: QualificationEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationEntries(store: QualificationEntryStore, query: QualificationEntryQuery): MutableList<QualificationEntry> {
    return query.apply(store.list())
}
