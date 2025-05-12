// Get all hobbies
exports.getAllHobbies = async (req, res) => {
    try {
      const pool = global.db
  
      const [hobbies] = await pool.query("SELECT * FROM hobbies ORDER BY name")
  
      res.status(200).send(hobbies)
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while retrieving hobbies.",
      })
    }
  }
  
  // Create a new hobby (admin only)
  exports.createHobby = async (req, res) => {
    try {
      const { name } = req.body
  
      // Validate request
      if (!name) {
        return res.status(400).send({
          message: "Hobby name is required!",
        })
      }
  
      const pool = global.db
  
      // Check if hobby already exists
      const [existingHobbies] = await pool.query("SELECT * FROM hobbies WHERE name = ?", [name])
  
      if (existingHobbies.length > 0) {
        return res.status(400).send({
          message: "Hobby already exists!",
        })
      }
  
      // Create hobby
      const [result] = await pool.query("INSERT INTO hobbies (name) VALUES (?)", [name])
  
      const hobbyId = result.insertId
  
      // Get the created hobby
      const [hobbies] = await pool.query("SELECT * FROM hobbies WHERE id = ?", [hobbyId])
  
      if (hobbies.length === 0) {
        return res.status(404).send({
          message: "Hobby not found after creation.",
        })
      }
  
      res.status(201).send({
        message: "Hobby created successfully",
        hobby: hobbies[0],
      })
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while creating the hobby.",
      })
    }
  }
  
  // Update hobby (admin only)
  exports.updateHobby = async (req, res) => {
    try {
      const hobbyId = req.params.id
      const { name } = req.body
  
      // Validate request
      if (!name) {
        return res.status(400).send({
          message: "Hobby name is required!",
        })
      }
  
      const pool = global.db
  
      // Check if hobby exists
      const [hobbies] = await pool.query("SELECT * FROM hobbies WHERE id = ?", [hobbyId])
  
      if (hobbies.length === 0) {
        return res.status(404).send({
          message: "Hobby not found.",
        })
        
      }
  
      // Check if new name already exists
      const [existingHobbies] = await pool.query("SELECT * FROM hobbies WHERE name = ? AND id != ?", [name, hobbyId])
  
      if (existingHobbies.length > 0) {
        return res.status(400).send({
          message: "Hobby name already exists!",
        })
      }
  
      // Update hobby
      await pool.query("UPDATE hobbies SET name = ? WHERE id = ?", [name, hobbyId])
  
      res.status(200).send({
        message: "Hobby updated successfully",
      })
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while updating the hobby.",
      })
    }
  }
  
  // Delete hobby (admin only)
  exports.deleteHobby = async (req, res) => {
    try {
      const hobbyId = req.params.id
      const pool = global.db
  
      // Check if hobby exists
      const [hobbies] = await pool.query("SELECT * FROM hobbies WHERE id = ?", [hobbyId])
  
      if (hobbies.length === 0) {
        return res.status(404).send({
          message: "Hobby not found.",
        })
      }
  
      // Delete hobby
      await pool.query("DELETE FROM hobbies WHERE id = ?", [hobbyId])
  
      res.status(200).send({
        message: "Hobby deleted successfully",
      })
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while deleting the hobby.",
      })
    }
  }
  