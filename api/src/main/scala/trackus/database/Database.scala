package trackus.database

import slick.jdbc.JdbcProfile

trait Database[Profile <: JdbcProfile] {
	val profile: JdbcProfile
	val database: Profile#Backend#Database
}