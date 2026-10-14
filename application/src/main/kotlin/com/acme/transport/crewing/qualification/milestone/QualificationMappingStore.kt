package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification mappings by code. */
interface QualificationMappingStore {
    fun find(code: String): QualificationMapping?
    fun save(item: QualificationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationMappingStore(
    var items: MutableMap<String, QualificationMapping>,
    var order: MutableList<String>,
) : QualificationMappingStore {
    override fun find(code: String): QualificationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationMapping) {
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

    override fun list(): MutableList<QualificationMapping> {
        val out = mutableListOf<QualificationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationMappingStore(): QualificationMappingStore {
    return MemoryQualificationMappingStore(
        mutableMapOf<String, QualificationMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationMappingStore(
    var inner: QualificationMappingStore,
    var cache: TtlCache<QualificationMapping>,
    var hits: Int,
    var misses: Int,
) : QualificationMappingStore {
    override fun find(code: String): QualificationMapping? {
        val hit = (cache.get(code) as QualificationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationMapping> = inner.list()
}

fun newCachedQualificationMappingStore(inner: QualificationMappingStore, ttl: Int, clock: Clock): QualificationMappingStore {
    return CachedQualificationMappingStore(
        inner = inner,
        cache = newTtlCache<QualificationMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification mappings. Empty fields match all. */
data class QualificationMappingQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationMapping>): MutableList<QualificationMapping> {
        val matched = filter(items) { candidate: QualificationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationMappings(store: QualificationMappingStore, query: QualificationMappingQuery): MutableList<QualificationMapping> {
    return query.apply(store.list())
}
